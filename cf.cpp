#include <algorithm>
#include <cctype>
#include <filesystem>
#include <fstream>
#include <iostream>
#include <optional>
#include <regex>
#include <sstream>
#include <stdexcept>
#include <string>
#include <vector>

namespace fs = std::filesystem;

class MarkdownFile {
public:
    MarkdownFile(fs::path source, fs::path relativePath)
        : sourcePath_(std::move(source)), relativePath_(std::move(relativePath)) {}

    const fs::path& sourcePath() const { return sourcePath_; }
    const fs::path& relativePath() const { return relativePath_; }

    std::vector<std::string> readLines() const {
        std::ifstream input(sourcePath_);
        if (!input) {
            throw std::runtime_error("Unable to read Markdown file: " + sourcePath_.string());
        }

        std::vector<std::string> lines;
        std::string line;
        while (std::getline(input, line)) {
            if (!line.empty() && line.back() == '\r') {
                line.pop_back();
            }
            lines.push_back(line);
        }
        return lines;
    }

private:
    fs::path sourcePath_;
    fs::path relativePath_;
};

class FileSystemValidator {
public:
    static void validateSourceDirectory(const fs::path& source) {
        if (!fs::exists(source)) {
            throw std::runtime_error("Source directory does not exist: " + source.string());
        }
        if (!fs::is_directory(source)) {
            throw std::runtime_error("Source path is not a directory: " + source.string());
        }
    }

    static void prepareOutputDirectory(const fs::path& output) {
        std::error_code error;
        fs::create_directories(output, error);
        if (error) {
            throw std::runtime_error("Unable to create output directory: " +
                                     output.string() + " (" + error.message() + ")");
        }
    }
};

class MarkdownScanner {
public:
    explicit MarkdownScanner(fs::path sourceRoot) : sourceRoot_(std::move(sourceRoot)) {}

    std::vector<MarkdownFile> scan() const {
        std::vector<MarkdownFile> files;
        for (const auto& entry : fs::recursive_directory_iterator(sourceRoot_)) {
            if (!entry.is_regular_file() || entry.path().extension() != ".md") {
                continue;
            }
            files.emplace_back(entry.path(), fs::relative(entry.path(), sourceRoot_));
        }
        std::sort(files.begin(), files.end(),
                  [](const MarkdownFile& left, const MarkdownFile& right) {
                      return left.relativePath() < right.relativePath();
                  });
        return files;
    }

private:
    fs::path sourceRoot_;
};

class HtmlEscaper {
public:
    static std::string escape(const std::string& text) {
        std::string escaped;
        escaped.reserve(text.size());
        for (const char character : text) {
            switch (character) {
            case '&':
                escaped += "&amp;";
                break;
            case '<':
                escaped += "&lt;";
                break;
            case '>':
                escaped += "&gt;";
                break;
            case '"':
                escaped += "&quot;";
                break;
            default:
                escaped += character;
            }
        }
        return escaped;
    }
};

class InlineFormatter {
public:
    std::string format(const std::string& text) const {
        static const std::regex boldPattern(R"(\*\*(.+?)\*\*)");
        std::string result;
        std::size_t cursor = 0;

        for (std::sregex_iterator match(text.begin(), text.end(), boldPattern), end;
             match != end; ++match) {
            const auto& current = *match;
            result += HtmlEscaper::escape(text.substr(cursor, current.position() - cursor));
            result += "<strong>" + HtmlEscaper::escape(current[1].str()) + "</strong>";
            cursor = current.position() + current.length();
        }
        result += HtmlEscaper::escape(text.substr(cursor));
        return result;
    }
};

class MarkdownParser {
public:
    std::string convert(const std::vector<std::string>& lines) const {
        std::ostringstream html;
        std::string paragraph;

        for (const std::string& line : lines) {
            if (line.empty()) {
                flushParagraph(paragraph, html);
                continue;
            }

            const auto heading = parseHeading(line);
            if (heading.has_value()) {
                flushParagraph(paragraph, html);
                html << "<h" << heading->first << ">"
                     << inlineFormatter_.format(heading->second)
                     << "</h" << heading->first << ">\n";
            } else {
                if (!paragraph.empty()) {
                    paragraph += " ";
                }
                paragraph += line;
            }
        }
        flushParagraph(paragraph, html);
        return html.str();
    }

private:
    std::optional<std::pair<int, std::string>> parseHeading(const std::string& line) const {
        std::size_t hashes = 0;
        while (hashes < line.size() && line[hashes] == '#') {
            ++hashes;
        }
        if (hashes == 0 || hashes > 6 || hashes == line.size() ||
            !std::isspace(static_cast<unsigned char>(line[hashes]))) {
            return std::nullopt;
        }

        std::size_t contentStart = hashes;
        while (contentStart < line.size() &&
               std::isspace(static_cast<unsigned char>(line[contentStart]))) {
            ++contentStart;
        }
        return std::make_pair(static_cast<int>(hashes), line.substr(contentStart));
    }

    void flushParagraph(std::string& paragraph, std::ostringstream& html) const {
        if (paragraph.empty()) {
            return;
        }
        html << "<p>" << inlineFormatter_.format(paragraph) << "</p>\n";
        paragraph.clear();
    }

    InlineFormatter inlineFormatter_;
};

class HtmlDocument {
public:
    static std::string wrap(const std::string& body, const std::string& title) {
        return "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n"
               "  <meta charset=\"UTF-8\">\n"
               "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
               "  <title>" + HtmlEscaper::escape(title) + "</title>\n"
               "</head>\n<body>\n" + body + "</body>\n</html>\n";
    }
};

class SiteWriter {
public:
    explicit SiteWriter(fs::path outputRoot) : outputRoot_(std::move(outputRoot)) {}

    void write(const MarkdownFile& markdown, const std::string& html) const {
        fs::path outputPath = outputRoot_ / markdown.relativePath();
        outputPath.replace_extension(".html");
        fs::create_directories(outputPath.parent_path());

        std::ofstream output(outputPath);
        if (!output) {
            throw std::runtime_error("Unable to write HTML file: " + outputPath.string());
        }
        output << html;
    }

private:
    fs::path outputRoot_;
};

class StaticSiteGenerator {
public:
    StaticSiteGenerator(fs::path source, fs::path output)
        : sourceRoot_(std::move(source)), outputRoot_(std::move(output)),
          scanner_(sourceRoot_), writer_(outputRoot_) {}

    std::size_t generate() const {
        FileSystemValidator::validateSourceDirectory(sourceRoot_);
        FileSystemValidator::prepareOutputDirectory(outputRoot_);

        const auto files = scanner_.scan();
        for (const auto& markdown : files) {
            const auto body = parser_.convert(markdown.readLines());
            const auto title = markdown.relativePath().stem().string();
            writer_.write(markdown, HtmlDocument::wrap(body, title));
        }
        return files.size();
    }

private:
    fs::path sourceRoot_;
    fs::path outputRoot_;
    MarkdownScanner scanner_;
    MarkdownParser parser_;
    SiteWriter writer_;
};

int main(int argc, char* argv[]) {
    if (argc != 3) {
        std::cerr << "Usage: " << argv[0] << " <source-directory> <output-directory>\n";
        return 1;
    }

    try {
        StaticSiteGenerator generator(argv[1], argv[2]);
        const std::size_t generated = generator.generate();
        std::cout << "Generated " << generated << " HTML file(s).\n";
        return 0;
    } catch (const std::exception& error) {
        std::cerr << "Error: " << error.what() << '\n';
        return 1;
    }
}

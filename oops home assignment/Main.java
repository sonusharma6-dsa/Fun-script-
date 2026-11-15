import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class MarkdownFile {
    private final Path sourcePath;
    private final Path relativePath;

    MarkdownFile(Path sourcePath, Path relativePath) {
        this.sourcePath = sourcePath;
        this.relativePath = relativePath;
    }

    Path getSourcePath() {
        return sourcePath;
    }

    Path getRelativePath() {
        return relativePath;
    }

    List<String> readLines() {
        try {
            return Files.readAllLines(sourcePath, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new SiteGenerationException(
                    "Unable to read Markdown file: " + sourcePath, exception);
        }
    }
}

class FileSystemValidator {
    static void validateSourceDirectory(Path source) {
        if (!Files.exists(source)) {
            throw new SiteGenerationException(
                    "Source directory does not exist: " + source);
        }
        if (!Files.isDirectory(source)) {
            throw new SiteGenerationException(
                    "Source path is not a directory: " + source);
        }
    }

    static void prepareOutputDirectory(Path output) {
        try {
            Files.createDirectories(output);
        } catch (IOException exception) {
            throw new SiteGenerationException(
                    "Unable to create output directory: " + output, exception);
        }
    }
}

class MarkdownScanner {
    private final Path sourceRoot;

    MarkdownScanner(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    List<MarkdownFile> scan() {
        try {
            return Files.walk(sourceRoot)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".md"))
                    .map(path -> new MarkdownFile(path, sourceRoot.relativize(path)))
                    .sorted(Comparator.comparing(file -> file.getRelativePath().toString()))
                    .collect(Collectors.toList());
        } catch (IOException exception) {
            throw new SiteGenerationException(
                    "Unable to scan source directory: " + sourceRoot, exception);
        }
    }
}

class HtmlEscaper {
    static String escape(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}

class InlineFormatter {
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");

    String format(String text) {
        Matcher matcher = BOLD_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        int cursor = 0;

        while (matcher.find()) {
            result.append(HtmlEscaper.escape(text.substring(cursor, matcher.start())));
            result.append("<strong>")
                    .append(HtmlEscaper.escape(matcher.group(1)))
                    .append("</strong>");
            cursor = matcher.end();
        }
        result.append(HtmlEscaper.escape(text.substring(cursor)));
        return result.toString();
    }
}

class MarkdownParser {
    private final InlineFormatter inlineFormatter = new InlineFormatter();

    String convert(List<String> lines) {
        StringBuilder html = new StringBuilder();
        StringBuilder paragraph = new StringBuilder();

        for (String line : lines) {
            if (line.isEmpty()) {
                flushParagraph(paragraph, html);
                continue;
            }

            Optional<Heading> heading = parseHeading(line);
            if (heading.isPresent()) {
                flushParagraph(paragraph, html);
                Heading value = heading.get();
                html.append("<h").append(value.level)
                        .append(">")
                        .append(inlineFormatter.format(value.text))
                        .append("</h").append(value.level).append(">\n");
            } else {
                if (paragraph.length() > 0) {
                    paragraph.append(' ');
                }
                paragraph.append(line);
            }
        }

        flushParagraph(paragraph, html);
        return html.toString();
    }

    private Optional<Heading> parseHeading(String line) {
        int hashes = 0;
        while (hashes < line.length() && line.charAt(hashes) == '#') {
            hashes++;
        }

        if (hashes == 0 || hashes > 6 || hashes == line.length()
                || !Character.isWhitespace(line.charAt(hashes))) {
            return Optional.empty();
        }

        int contentStart = hashes;
        while (contentStart < line.length()
                && Character.isWhitespace(line.charAt(contentStart))) {
            contentStart++;
        }
        return Optional.of(new Heading(hashes, line.substring(contentStart)));
    }

    private void flushParagraph(StringBuilder paragraph, StringBuilder html) {
        if (paragraph.length() == 0) {
            return;
        }
        html.append("<p>")
                .append(inlineFormatter.format(paragraph.toString()))
                .append("</p>\n");
        paragraph.setLength(0);
    }

    private static class Heading {
        private final int level;
        private final String text;

        Heading(int level, String text) {
            this.level = level;
            this.text = text;
        }
    }
}

class HtmlDocument {
    static String wrap(String body, String title) {
        return "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <title>" + HtmlEscaper.escape(title) + "</title>\n"
                + "</head>\n"
                + "<body>\n"
                + body
                + "</body>\n"
                + "</html>\n";
    }
}

class SiteWriter {
    private final Path outputRoot;

    SiteWriter(Path outputRoot) {
        this.outputRoot = outputRoot;
    }

    void write(MarkdownFile markdown, String html) {
        Path outputPath = outputRoot.resolve(markdown.getRelativePath());
        String fileName = outputPath.getFileName().toString();
        outputPath = outputPath.resolveSibling(
                fileName.substring(0, fileName.length() - 3) + ".html");

        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, html, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new SiteGenerationException(
                    "Unable to write HTML file: " + outputPath, exception);
        }
    }
}

class StaticSiteGenerator {
    private final Path sourceRoot;
    private final Path outputRoot;
    private final MarkdownScanner scanner;
    private final MarkdownParser parser = new MarkdownParser();
    private final SiteWriter writer;

    StaticSiteGenerator(Path sourceRoot, Path outputRoot) {
        this.sourceRoot = sourceRoot;
        this.outputRoot = outputRoot;
        this.scanner = new MarkdownScanner(sourceRoot);
        this.writer = new SiteWriter(outputRoot);
    }

    int generate() {
        FileSystemValidator.validateSourceDirectory(sourceRoot);
        FileSystemValidator.prepareOutputDirectory(outputRoot);

        List<MarkdownFile> files = scanner.scan();
        for (MarkdownFile markdown : files) {
            String body = parser.convert(markdown.readLines());
            String title = removeExtension(
                    markdown.getRelativePath().getFileName().toString());
            writer.write(markdown, HtmlDocument.wrap(body, title));
        }
        return files.size();
    }

    private String removeExtension(String fileName) {
        return fileName.substring(0, fileName.length() - 3);
    }
}

class SiteGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    SiteGenerationException(String message) {
        super(message);
    }

    SiteGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(
                    "Usage: java Main <source-directory> <output-directory>");
            System.exit(1);
        }

        try {
            Path source = Paths.get(args[0]);
            Path output = Paths.get(args[1]);
            int generated = new StaticSiteGenerator(source, output).generate();
            System.out.println("Generated " + generated + " HTML file(s).");
        } catch (SiteGenerationException exception) {
            System.err.println("Error: " + exception.getMessage());
            System.exit(1);
        }
    }
}

// PART 3 - POLYMORPHIC TRANSLATION ENGINE
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class StaticSiteGenerator {
    private final Path sourceRoot;
    private final Path outputRoot;
    private final MarkdownScanner scanner;
    private final MarkdownParser parser;
    private final SiteWriter writer;

    public StaticSiteGenerator(Path sourceRoot, Path outputRoot) {
        this.sourceRoot = sourceRoot;
        this.outputRoot = outputRoot;
        this.scanner = new MarkdownScanner(sourceRoot);
        this.parser = new MarkdownParser();
        this.writer = new SiteWriter(outputRoot);
    }

    public int generate() {
        FileSystemValidator.validateSourceDirectory(sourceRoot);
        FileSystemValidator.prepareOutputDirectory(outputRoot);

        List<MarkdownFile> files = scanner.scan();
        for (MarkdownFile markdown : files) {
            List<ElementNode> elements = parser.parse(markdown.readLines());
            String body = elements.stream()
                    .map(ElementNode::render)
                    .collect(Collectors.joining());
            String title = removeExtension(
                    markdown.getRelativePath().getFileName().toString());
            HTMLDocument document = new HTMLDocument(title, standardHeader(), body);
            writer.write(markdown, document);
        }
        return files.size();
    }

    private String standardHeader() {
        return "  <style>\n"
                + "    body { font-family: Arial, sans-serif; line-height: 1.6; "
                + "max-width: 900px; margin: 2rem auto; padding: 0 1rem; }\n"
                + "    h1, h2, h3 { color: #333; }\n"
                + "  </style>\n";
    }

    private String removeExtension(String fileName) {
        return fileName.substring(0, fileName.length() - 3);
    }
}

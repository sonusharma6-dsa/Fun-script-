// PART 5 - FILE I/O
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SiteWriter {
    private final Path outputRoot;

    public SiteWriter(Path outputRoot) {
        this.outputRoot = outputRoot;
    }

    public void write(MarkdownFile markdown, HTMLDocument document) {
        Path outputPath = outputRoot.resolve(markdown.getRelativePath());
        String fileName = outputPath.getFileName().toString();
        outputPath = outputPath.resolveSibling(
                fileName.substring(0, fileName.length() - 3) + ".html");

        try {
            Files.createDirectories(outputPath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(
                    outputPath, StandardCharsets.UTF_8)) {
                writer.write(document.render());
            }
        } catch (IOException exception) {
            throw new SiteGenerationException(
                    "Unable to write HTML file: " + outputPath, exception);
        }
    }
}

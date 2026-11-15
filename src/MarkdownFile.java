// PART 5 - FILE I/O
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MarkdownFile {
    private final Path sourcePath;
    private final Path relativePath;

    public MarkdownFile(Path sourcePath, Path relativePath) {
        this.sourcePath = sourcePath;
        this.relativePath = relativePath;
    }

    public Path getRelativePath() {
        return relativePath;
    }

    public List<String> readLines() {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(
                sourcePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException exception) {
            throw new SiteGenerationException(
                    "Unable to read Markdown file: " + sourcePath, exception);
        }
        return lines;
    }
}

// PART 5 - FILE COLLECTIONS AND STREAMS
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkdownScanner {
    private final Path sourceRoot;

    public MarkdownScanner(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    public List<MarkdownFile> scan() {
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".md"))
                    .map(path -> new MarkdownFile(path, sourceRoot.relativize(path)))
                    .sorted(Comparator.comparing(
                            file -> file.getRelativePath().toString()))
                    .collect(Collectors.toList());
        } catch (IOException exception) {
            throw new SiteGenerationException(
                    "Unable to scan source directory: " + sourceRoot, exception);
        }
    }
}

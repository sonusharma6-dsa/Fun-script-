// PART 7 - VALIDATION AND EXPLICIT ERRORS
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileSystemValidator {
    private FileSystemValidator() {
    }

    public static void validateSourceDirectory(Path source) {
        if (!Files.exists(source)) {
            throw new SiteGenerationException(
                    "Source directory does not exist: " + source);
        }
        if (!Files.isDirectory(source)) {
            throw new SiteGenerationException(
                    "Source path is not a directory: " + source);
        }
    }

    public static void prepareOutputDirectory(Path output) {
        try {
            Files.createDirectories(output);
        } catch (IOException exception) {
            throw new SiteGenerationException(
                    "Unable to create output directory: " + output, exception);
        }
    }
}

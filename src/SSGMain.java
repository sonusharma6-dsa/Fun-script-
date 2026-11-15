// PART 8 - COMMAND-LINE EXECUTION WORKFLOW
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SSGMain {
    public static void main(String[] args) {
        try (BufferedReader console = new BufferedReader(new InputStreamReader(
                System.in, StandardCharsets.UTF_8))) {
            System.out.print("Enter source directory path: ");
            String input = console.readLine();
            if (input == null || input.trim().isEmpty()) {
                throw new SiteGenerationException(
                        "A source directory path is required.");
            }

            Path source = Paths.get(input.trim());
            Path dist = Paths.get("dist");
            int generated = new StaticSiteGenerator(source, dist).generate();
            System.out.println("Generated " + generated + " HTML file(s) in " + dist + ".");
        } catch (IOException exception) {
            System.err.println("Error reading the source directory path: "
                    + exception.getMessage());
            System.exit(1);
        } catch (SiteGenerationException exception) {
            System.err.println("Error: " + exception.getMessage());
            System.exit(1);
        }
    }
}

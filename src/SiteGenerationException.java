// PART 7 - EXPLICIT APPLICATION EXCEPTION
public class SiteGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SiteGenerationException(String message) {
        super(message);
    }

    public SiteGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

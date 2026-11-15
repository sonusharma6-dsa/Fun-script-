// PART 6 - SAFE HTML OUTPUT
public final class HtmlEscaper {
    private HtmlEscaper() {
    }

    public static String escape(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

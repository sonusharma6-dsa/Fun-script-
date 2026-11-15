// PART 4 - ENCAPSULATION AND TEMPLATE
public class HTMLDocument {
    private final String title;
    private final String header;
    private final String compiledBody;

    public HTMLDocument(String title, String header, String compiledBody) {
        this.title = title;
        this.header = header;
        this.compiledBody = compiledBody;
    }

    public String getTitle() {
        return title;
    }

    public String getHeader() {
        return header;
    }

    public String getCompiledBody() {
        return compiledBody;
    }

    public String render() {
        return "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <title>" + HtmlEscaper.escape(title) + "</title>\n"
                + header
                + "</head>\n"
                + "<body>\n"
                + compiledBody
                + "</body>\n"
                + "</html>\n";
    }
}

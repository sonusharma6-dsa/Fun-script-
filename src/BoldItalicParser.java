// PART 2 - INHERITANCE: INLINE SYNTAX PARSER
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoldItalicParser implements ElementNode {
    private static final Pattern BOLD_PATTERN =
            Pattern.compile("\\*\\*(.+?)\\*\\*");

    private final String source;

    public BoldItalicParser(String source) {
        this.source = source;
    }

    @Override
    public String render() {
        Matcher matcher = BOLD_PATTERN.matcher(source);
        StringBuilder html = new StringBuilder();
        int cursor = 0;

        while (matcher.find()) {
            html.append(HtmlEscaper.escape(source.substring(cursor, matcher.start())));
            html.append("<strong>")
                    .append(HtmlEscaper.escape(matcher.group(1)))
                    .append("</strong>");
            cursor = matcher.end();
        }

        html.append(HtmlEscaper.escape(source.substring(cursor)));
        return html.toString();
    }
}

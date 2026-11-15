// PART 2 - INHERITANCE: BLOCK SYNTAX PARSER
public class HeadingParser implements ElementNode {
    private final int level;
    private final String text;

    public HeadingParser(int level, String text) {
        this.level = level;
        this.text = text;
    }

    @Override
    public String render() {
        return "<h" + level + ">"
                + new BoldItalicParser(text).render()
                + "</h" + level + ">\n";
    }
}

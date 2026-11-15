// PART 2 - INHERITANCE: BLOCK SYNTAX PARSER
public class ParagraphParser implements ElementNode {
    private final ElementNode inlineContent;

    public ParagraphParser(String text) {
        this.inlineContent = new BoldItalicParser(text);
    }

    @Override
    public String render() {
        return "<p>" + inlineContent.render() + "</p>\n";
    }
}

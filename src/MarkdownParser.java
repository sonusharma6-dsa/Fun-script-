// PART 3 - RUNTIME POLYMORPHISM
import java.util.ArrayList;
import java.util.List;

public class MarkdownParser {
    public List<ElementNode> parse(List<String> lines) {
        List<ElementNode> elements = new ArrayList<>();
        StringBuilder paragraph = new StringBuilder();

        for (String line : lines) {
            if (line.trim().isEmpty()) {
                addParagraph(elements, paragraph);
                continue;
            }

            int headingLevel = headingLevel(line);
            if (headingLevel > 0) {
                addParagraph(elements, paragraph);
                String headingText = line.substring(headingLevel).trim();
                elements.add(new HeadingParser(headingLevel, headingText));
            } else {
                if (paragraph.length() > 0) {
                    paragraph.append(' ');
                }
                paragraph.append(line);
            }
        }

        addParagraph(elements, paragraph);
        return elements;
    }

    private int headingLevel(String line) {
        int hashes = 0;
        while (hashes < line.length() && line.charAt(hashes) == '#') {
            hashes++;
        }
        if (hashes < 1 || hashes > 3 || hashes == line.length()
                || !Character.isWhitespace(line.charAt(hashes))) {
            return 0;
        }
        return hashes;
    }

    private void addParagraph(List<ElementNode> elements, StringBuilder paragraph) {
        if (paragraph.length() > 0) {
            elements.add(new ParagraphParser(paragraph.toString()));
            paragraph.setLength(0);
        }
    }
}

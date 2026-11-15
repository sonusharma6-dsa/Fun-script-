# Automated Markdown-to-HTML Static Site Generator

This Java command-line application converts Markdown files into HTML pages.
Generated pages are written to the `dist/` directory.

## Compile and run

From the project root, run:

```powershell
javac -d out src/*.java
java -cp out SSGMain
```

When prompted, enter the path of a directory containing Markdown files. For
example:

```text
markdown
```

The generated files will be placed in `dist/`. Nested Markdown directories are
preserved in the output structure.

## Supported Markdown

| Markdown | HTML |
| --- | --- |
| `# Heading` | `<h1>Heading</h1>` |
| `## Heading` | `<h2>Heading</h2>` |
| `### Heading` | `<h3>Heading</h3>` |
| Plain text | `<p>Plain text</p>` |
| `**bold**` | `<strong>bold</strong>` |

HTML special characters are escaped so source text such as `<`, `>`, and `&`
is displayed safely in the generated page.

## Java design

- `ElementNode` defines the common rendering abstraction.
- `HeadingParser`, `ParagraphParser`, and `BoldItalicParser` implement
  runtime-polymorphic rendering.
- `MarkdownParser` converts source lines into a `List<ElementNode>`.
- `HTMLDocument` encapsulates the title, template header, and compiled body.
- `MarkdownFile` and `SiteWriter` use `BufferedReader` and `BufferedWriter`.
- `MarkdownScanner` discovers `.md` files recursively using Java streams.

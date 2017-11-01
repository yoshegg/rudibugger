/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.tabs;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 * based on https://github.com/TomasMikula/RichTextFX/blob/master/richtextfx-demos/src/main/java/org/fxmisc/richtext/demo/JavaKeywords.java
 * but slightly modified.
 */
public class RudiCodeArea extends CodeArea {

  static Logger log = Logger.getLogger("rudiLog");

  private static final String[] KEYWORDS = new String[]{
    "abstract", "assert", "boolean", "break", "byte",
    "case", "catch", "char", "class", "const",
    "continue", "default", "do", "double", "else",
    "enum", "extends", "final", "finally", "float",
    "for", "goto", "if", "implements", "import",
    "instanceof", "int", "interface", "long", "native",
    "new", "package", "private", "protected", "public",
    "return", "short", "static", "strictfp", "super",
    "switch", "synchronized", "this", "throw", "throws",
    "transient", "try", "void", "volatile", "while"
  };

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String BRACE_PATTERN = "\\{|\\}";
  private static final String BRACKET_PATTERN = "\\[|\\]";
  private static final String SEMICOLON_PATTERN = "\\;";
  private static final String RULELABEL_PATTERN = "((?<=\\n)|(?<=^))([a-zA-Z0-9_])+(?=\\:)";
  private static final String IMPORT_PATTERN = "((?<=^import)|(?<=\\nimport))\\s+([a-zA-Z]|[0-9]|_)+(?=\\;)";
  private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
  private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

  private static final Pattern PATTERN = Pattern.compile(
          "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<PAREN>" + PAREN_PATTERN + ")"
          + "|(?<BRACE>" + BRACE_PATTERN + ")"
          + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
          + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
          + "|(?<STRING>" + STRING_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
          + "|(?<RULELABEL>" + RULELABEL_PATTERN + ")"
          + "|(?<IMPORT>" + IMPORT_PATTERN + ")"
  );

  public RudiCodeArea() {
    super();
  }

  public void initializeCodeArea() {
    this.setParagraphGraphicFactory(LineNumberFactory.get(this));
    this.richChanges()
            .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
            .subscribe(change -> {
              if (!"".equals(this.getText())) { // own fix to prevent IllegalStateException
                this.setStyleSpans(0, computeHighlighting(this.getText()));
              }
            });
    if (!"".equals(this.getText())) { // own fix to prevent IllegalStateException
      this.setStyleSpans(0, computeHighlighting(this.getText()));
    }
  }

  private static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
            = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass
              = matcher.group("KEYWORD") != null ? "keyword"
              : matcher.group("PAREN") != null ? "paren"
              : matcher.group("BRACE") != null ? "brace"
              : matcher.group("BRACKET") != null ? "bracket"
              : matcher.group("SEMICOLON") != null ? "semicolon"
              : matcher.group("STRING") != null ? "string"
              : matcher.group("COMMENT") != null ? "comment"
              : matcher.group("RULELABEL") != null ? "isRule"
              : matcher.group("IMPORT") != null ? "isImport"
              : null;
      /* never happens */ assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  public void showParagraphPretty(int line) {
    if (line > 4) {
      this.showParagraphAtTop(line - 5);
    } else {
      this.showParagraphAtTop(0);
    }
  }
}

/*
 * The Creative Commons CC-BY-NC 4.0 License
 *
 * http://creativecommons.org/licenses/by-nc/4.0/legalcode
 *
 * Creative Commons (CC) by DFKI GmbH
 *  - Bernd Kiefer <kiefer@dfki.de>
 *  - Anna Welker <anna.welker@dfki.de>
 *  - Christophe Biwer <christophe.biwer@dfki.de>
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.dfki.mlt.rudibugger.TabManagement;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.IndexRange;
import static javafx.scene.input.KeyCode.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import static org.fxmisc.wellbehaved.event.EventPattern.*;
import static org.fxmisc.wellbehaved.event.InputMap.*;
import org.fxmisc.wellbehaved.event.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 * based on https://github.com/TomasMikula/RichTextFX/blob/master/richtextfx-demos/src/main/java/org/fxmisc/richtext/demo/JavaKeywords.java
 * but slightly modified.
 */
public class RudiCodeArea extends CodeArea {

  static Logger log = LoggerFactory.getLogger("rudiCodeArea");

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
  private static final String RULELABEL_PATTERN = "((?<=\\n)|(?<=^)) *([a-zA-Z0-9_])+(?=\\:)";
  private static final String IMPORT_PATTERN = "((?<=^import)|(?<=\\nimport)) +[a-zA-Z0-9_\\.]+(?=\\;)";
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
    init();
  }

  private void shiftRightManagement() {
    int initialCaretPosition = this.getCaretPosition();

    IndexRange selection = this.getSelection();

    this.moveTo(selection.getStart());
    int startCol = this.getCurrentParagraph();
    this.moveTo(startCol, 0);
    int startPos = this.getCaretPosition();
    int endPos = selection.getEnd();
    this.moveTo(endPos);
    int endCol = this.getCurrentParagraph();

    String toBeProcessedText = this.getText(startPos, endPos);

    String x = "  " + toBeProcessedText.replaceAll("\n", "\n  ");
    IndexRange tempSelection = new IndexRange(startPos, endPos);
    this.replaceText(tempSelection, x);

    if (selection.getStart() < initialCaretPosition) {
      int adaptToShift = 2 * (endCol - startCol);
      this.selectRange(selection.getStart() + 2,
        initialCaretPosition + adaptToShift + 2);
    } else {
      int adaptToShift = 2 * (endCol - startCol);
      this.selectRange(endPos + 2 + adaptToShift, initialCaretPosition + 2);
    }

  }

  private void init() {
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
    Nodes.addInputMap(this, sequence(
            consume(keyPressed(TAB), e -> shiftRightManagement())
    ));
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

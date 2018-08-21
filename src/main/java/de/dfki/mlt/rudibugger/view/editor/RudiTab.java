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

package de.dfki.mlt.rudibugger.view.editor;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.editor.Editor;
import de.dfki.mlt.rudibugger.editor.RudibuggerEditor;
import de.dfki.mlt.rudibugger.HelperWindows;
import java.nio.file.Path;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 * A <code>RudiTab</code> is a tab containing <code>.rudi</code> code and
 * showing it with special syntax highlighting.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiTab extends Tab {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("rudiLog");


  /*****************************************************************************
   * FIELDS & PROPERTIES
   ****************************************************************************/

  private final RudibuggerEditor _editor;

  /** Represents the associated file. */
  private Path _file;

  /** Represents the codeArea showing the current <code>.rudi</code> code. */
  protected RudiCodeArea _codeArea;

  /** Indicates if the content of a tab has been modified and not saved yet. */
  private final BooleanProperty hasBeenModified = new SimpleBooleanProperty();

  /** Represents the main <code>AnchorPane</code> that is shown in the tab. */
  private AnchorPane _mainAnchorPane;

  /** Listens to modifications of the <code>codeArea</code>. */
  private ChangeListener contentChangeListener = new ChangeListener() {
    @Override
    public void changed(ObservableValue o, Object oldValue, Object newValue) {
      setText("*" + RudiTab.this.getText());
      hasBeenModified.setValue(true);
      o.removeListener(contentChangeListener);
    }
  };

  /*****************************************************************************
   * CONSTRUCTORS
   ****************************************************************************/

  public RudiTab(String content, Path file, RudibuggerEditor editor) {
    _editor = editor;
    _file = file;
    _codeArea = new RudiCodeArea();
    _codeArea.appendText(content);
    initLayout();
    waitForModifications();
    setClosingBehaviour();
  }

  /** Creates a new tab and links a file to it. */
  public RudiTab(String content, RudibuggerEditor editor) {
    _editor = editor;
    _codeArea = new RudiCodeArea();
    _codeArea.appendText(content);
    initLayout();
    waitForModifications();
    setClosingBehaviour();
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Initializes the layout. */
  private void initLayout() {

    /* Set the title of the tab. */
    if (_file == null)
      setText("Untitled RudiTab");
    else
      setText(_file.getFileName().toString());

    /* Add Scrollbar to tab's content. */
    VirtualizedScrollPane textAreaWithScrollBar
            = new VirtualizedScrollPane<>(_codeArea);

    /* Set TextArea to fit parent VirtualizedScrollPane. */
    AnchorPane.setTopAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane.setRightAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane.setLeftAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane.setBottomAnchor(textAreaWithScrollBar, 0.0);
    _mainAnchorPane = new AnchorPane(textAreaWithScrollBar);

    /* Set CSS. */
    try {
      _mainAnchorPane.getStylesheets().add("/styles/rudi-keywords.css");
    } catch (NullPointerException e) {
      log.error("The provided .css file could not be found.");
    }

    /* Link content to tab. */
    setContent(_mainAnchorPane);

  }

  /**
   * Resets the field indicating that a file has unsaved changes and starts
   * listening for possible changes.
   */
  public final void waitForModifications() {
    hasBeenModified.setValue(false);
    setText(_file.getFileName().toString());
    _codeArea.textProperty().addListener(contentChangeListener);
  }

  private void setClosingBehaviour() {
    this.setOnCloseRequest(e -> {
      while (true) {

        if (isModified()) {
          String fileName = (_file != null) ? _file.getFileName().toString()
                  : "Untitled file";
          int returnValue =
            HelperWindows.openCloseFileWithoutSavingCheckDialog(fileName);
          switch (returnValue) {
            case CLOSE_BUT_SAVE_FIRST:
              _editor.saveFile(this);
              _editor.closeTab(this);
              continue; // continue with while
            case CLOSE_WITHOUT_SAVING:
              _editor.closeTab(this);
              break; // break out of switch
            case CANCEL_CLOSING:
              e.consume();
              break; // break out of switch
          }
          break; // break out of while
        } else {
           _editor.closeTab(this);
          break; // break out of while
        }
      }
    });
  }


  /*****************************************************************************
   * GETTERS & SETTERS
   ****************************************************************************/

  /** @return False, if the tab's content has never been saved, else true. */
  public Boolean isKnown() { return _file != null; }

  /** @return The associated file of this tab. */
  public Path getFile() { return _file; }

  /**
   * Sets the file of this tab.
   *
   * @param file
   *        The associated file.
   */
  public void setFile(Path file) { _file = file; }

  /** @return The <code>.rudi</code> code shown in the tab. */
  public String getRudiCode() { return _codeArea.getText(); }

  /** @return The codeArea showing the <code>.rudi</code> code. */
  public RudiCodeArea getCodeArea() { return _codeArea; }

  /**
   * @return Property indicating if the content of a tab has been modified and
   * not saved.
   */
  public BooleanProperty hasBeenModifiedProperty() { return hasBeenModified; }

  public Boolean isModified() { return hasBeenModified.get(); }

}

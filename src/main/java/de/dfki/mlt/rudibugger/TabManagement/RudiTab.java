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

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.HelperWindows;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Scanner;
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

  /** Represents the associated file. */
  private Path _file;

  /** Represents the codeArea showing the current <code>.rudi</code> code. */
  protected RudiCodeArea _codeArea;

  /** Indicates whether or not a file has already been saved once. */
  private Boolean _isKnown;

  /** Indicates if the content of a tab has been modified and not saved yet. */
  private final BooleanProperty hasBeenModified = new SimpleBooleanProperty();

  /** Represents the main <code>AnchorPane</code> that is shown in the tab. */
  private AnchorPane _mainAnchorPane;

  /**
   * Represents the <code>TabStoreView</code> that manages the view of the tabs.
   */
  private TabStoreView _tabStoreView;

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

  /** Creates a new empty tab. */
  public RudiTab() {
    super();
  }

  /** Creates a new tab and links a file to it. */
  public RudiTab(Path file, TabStoreView tsv) {
    this();
    _file = file;
    _tabStoreView = tsv;
    initCodeArea();
    initLayout();
    waitForModifications();
    setClosingBehaviour();
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Reads in a possibly provided file and sets up the <code>codeArea</code>. */
  private void initCodeArea() {

    /* Create a codeArea. */
    _codeArea = new RudiCodeArea();
    _codeArea.initializeCodeArea();

    /* Read in file (if provided). */
    if (_file != null) {
      try {
        Scanner s = new Scanner(_file.toFile()).useDelimiter("\n");
        while (s.hasNext()) {
          _codeArea.appendText(s.next() + "\n");
        }
      } catch (FileNotFoundException e) {
        log.error("Something went wrong while reading in "
                + _file.getFileName().toString());
      }
      _isKnown = true;
    } else {
      _isKnown = false;
    }
  }


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
  public void waitForModifications() {
    hasBeenModified.setValue(false);
    _codeArea.textProperty().addListener(contentChangeListener);
  }

  private void setClosingBehaviour() {
    this.setOnCloseRequest(e -> {
      while (true) {

        if (hasBeenModified.get()) {
          String fileName = (_file != null) ? _file.getFileName().toString()
                  : "Untitled file";
          int returnValue = HelperWindows.closeFileWithoutSavingCheck(fileName);
          switch (returnValue) {
            case CLOSE_BUT_SAVE_FIRST:
              _tabStoreView.requestedSavingOfTabProperty().set(this);
              /*
               * Immediately reset this listener;
               * probably needed because of a JavaFX bug.
               */
              _tabStoreView.requestedSavingOfTabProperty().set(null);
              continue; // continue with while
            case CLOSE_WITHOUT_SAVING:
              _tabStoreView.closeTab(this);
              break; // break out of switch
            case CANCEL_CLOSING:
              e.consume();
              break; // break out of switch
          }
          break; // break out of while
        } else {
          _tabStoreView.closeTab(this);
          break; // break out of while
        }
      }
    });
  }


  /*****************************************************************************
   * GETTERS & SETTERS
   ****************************************************************************/

  /** @return False, if the tab's content has never been saved, else true. */
  public Boolean isKnown() { return _isKnown; }

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

}

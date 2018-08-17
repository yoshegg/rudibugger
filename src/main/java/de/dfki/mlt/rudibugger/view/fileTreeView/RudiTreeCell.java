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

package de.dfki.mlt.rudibugger.view.fileTreeView;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.HashSet;
import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.project.Project;
import static de.dfki.mlt.rudimant.common.ErrorInfo.ErrorType.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseButton;

/**
 * This TreeCell is used to visualize the different .rudi files according to
 * their usage stage in the current project.
 *
 * An entry may be
 *   - the main file,
 *   - the wrapper file,
 *   - a normal module used or not used in the current compiled project, or
 *   - a folder.
 *
 * A file that has been modified since the last successful compilation will have
 * a different background colour.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiTreeCell extends TreeCell<RudiPath> {

  /*****************************************************************************
   * CONSTANTS
   ****************************************************************************/

  /** Icon path of files. */
  static final String ICON_PATH_FILES
          = "file:src/main/resources/icons/FilesAndFolders/";

  /** Map of file icons. */
  static final HashMap<Integer, Image> ICONS_FILES
          = new HashMap<Integer, Image>() {{
    put(FILE_IS_MAIN,    new Image(ICON_PATH_FILES + "main.png"));
    put(FILE_IS_WRAPPER, new Image(ICON_PATH_FILES + "wrapper.png"));
    put(FILE_USED,       new Image(ICON_PATH_FILES + "enabled.png"));
    put(FILE_NOT_USED,   new Image(ICON_PATH_FILES + "disabled.png"));
    put(IS_FOLDER,       new Image(ICON_PATH_FILES + "folder.png"));
  }};


  /*****************************************************************************
   * FIELDS
   ****************************************************************************/

  /** TODO */
  private final Project _project;


  /*****************************************************************************
   * PSEUDOCLASSES
   ****************************************************************************/

  /** Used to visually distinguish modified files with CSS. */
  private final PseudoClass modifiedFileClass
          = PseudoClass.getPseudoClass("modifiedFile");

  /** Used to visually distinguish erroneous imports with CSS. */
  private final PseudoClass errorsInImportClass
          = PseudoClass.getPseudoClass("errorsInImport");

  /** Used to visually distinguish imports with warnings with CSS. */
  private final PseudoClass warningsInImportClass
          = PseudoClass.getPseudoClass("warningsInImport");

  /** Used to visually distinguish erroneous, modified imports with CSS. */
  private final PseudoClass modifiedAndErrorsInImportClass
          = PseudoClass.getPseudoClass("modifiedAndErrorsInImport");

  /** Used to visually distinguish modified imports with warnings with CSS. */
  private final PseudoClass modifiedAndWarningsInImportClass
          = PseudoClass.getPseudoClass("modifiedAndWarningsInImport");

  /** Contains all <code>PseudoClass</code>es. */
  private final Set<PseudoClass> _pseudoClasses = new HashSet<PseudoClass>() {{
    add(modifiedFileClass);
    add(errorsInImportClass);
    add(warningsInImportClass);
    add(modifiedAndErrorsInImportClass);
    add(modifiedAndWarningsInImportClass);
  }};


  /*****************************************************************************
   * LISTENERS
   ****************************************************************************/

  /** Used to listen to modification changes. */
  private final ChangeListener<Boolean> modificationListener = (o, ov, nv)
          -> setPseudoClass(nv);

  /** Used to listen to usage state changes. */
  private final ChangeListener<Number> usageStateListener = (o, ov, nv)
          -> this.setGraphic(new ImageView(ICONS_FILES.get(nv.intValue())));



  /*****************************************************************************
   * CONSTRUCTOR
   ****************************************************************************/

  /** Initializes a new cell. */
  public RudiTreeCell(Project project) {
    super();
    _project = project;
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  @Override
  protected void updateItem(RudiPath rudiPath, boolean empty) {

    /* Remove old listener from the cell */
    RudiPath oldItem = getItem();
    if (oldItem != null) {
      oldItem.modifiedProperty().removeListener(modificationListener);
      oldItem.usedProperty().removeListener(usageStateListener);
    }

    super.updateItem(rudiPath, empty);

    if (empty || rudiPath == null) {

      setText(null);
      setGraphic(null);

      _pseudoClasses.forEach(e -> pseudoClassStateChanged(e, false));

    } else {

      /* Set the correct pseudoClass for the background of the cell */
      setPseudoClass(rudiPath.modifiedProperty().getValue());
      rudiPath.modifiedProperty().addListener(modificationListener);

      /* Set label of TreeItem. */
      setText(rudiPath.toString());

      /* Define icon and listener for icon. */
      setGraphic(new ImageView(ICONS_FILES.get(
              rudiPath.usedProperty().getValue())));
      rudiPath.usedProperty().addListener(usageStateListener);

      /* Define click actions. */
      defineContextMenu(rudiPath);
      defineDoubleClickBehaviour(rudiPath);

    }
  }

  /**
   * Defines the <code>ContextMenu</code>.
   *
   * @param rudiPath The associated file of this cell
   */
  private void defineContextMenu(RudiPath rudiPath) {
    this.setOnContextMenuRequested(e -> {
        if (! Files.isDirectory(rudiPath.getPath())) {
          RudiContextMenu rcm = new RudiContextMenu(rudiPath, _project);
          rcm.show(this, e.getScreenX(), e.getScreenY());
        }
      });
  }

  /**
   * Defines the double click behaviour.
   *
   * @param rudiPath The associated file of this cell
   */
  private void defineDoubleClickBehaviour(RudiPath rudiPath) {
    this.setOnMouseClicked(e -> {
      if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY)
        if (! Files.isDirectory(rudiPath.getPath()))
          _project.openFile(rudiPath.getPath());
    });
  }

  /**
   * Sets the needed pseudoClass to the given state and all the other to false.
   * Only one pseudoClass should be true at the same time.
   *
   * @param modified True, if the file has been modified, else false.
   */
  private void setPseudoClass(boolean modified) {
    _pseudoClasses.forEach(e -> pseudoClassStateChanged(e, false));
    if (this.getItem().getImportInfo() != null) {
      if (modified) {  // has been modified
        if (hasErrors())
          pseudoClassStateChanged(modifiedAndErrorsInImportClass, true);
        else if (hasWarnings())
          pseudoClassStateChanged(modifiedAndWarningsInImportClass, true);
        else
          pseudoClassStateChanged(modifiedFileClass, true);
      } else {  // has not been modified
        if (hasErrors())
          pseudoClassStateChanged(errorsInImportClass, true);
        else if (hasWarnings())
          pseudoClassStateChanged(warningsInImportClass, true);
        else
          pseudoClassStateChanged(modifiedFileClass, false);
      }
    } else
      pseudoClassStateChanged(modifiedFileClass, modified);
  }

  /** @return True, if errors occurred with this file, else false. */
  private boolean hasErrors() {
    return this.getItem().getImportInfo().getErrors().stream().anyMatch(
                ewi -> ewi.getType() == ERROR || ewi.getType() == PARSE_ERROR);
  }

  /** @return True, if warnings occurred with this file, else false. */
  private boolean hasWarnings() {
    return this.getItem().getImportInfo().getErrors().stream().anyMatch(
                ewi -> ewi.getType() == WARNING);
  }

}

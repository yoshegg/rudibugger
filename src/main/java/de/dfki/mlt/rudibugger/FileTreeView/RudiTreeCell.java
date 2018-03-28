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

package de.dfki.mlt.rudibugger.FileTreeView;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static de.dfki.mlt.rudibugger.Constants.*;
import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.TreeCell;

/**
 * This TreeCell is used to visualize the different .rudi files according to
 * their usage stage in the current project.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiTreeCell extends TreeCell<RudiPath> {

  /** Icon path of files. */
  static final String ICON_PATH_FILES
          = "file:src/main/resources/icons/FilesAndFolders/";

  /** Map of file icons. */
  static final HashMap<Integer, Image> ICONS_FILES = new HashMap<Integer, Image>() {{
    put(FILE_IS_MAIN,    new Image(ICON_PATH_FILES + "main.png"));
    put(FILE_IS_WRAPPER, new Image(ICON_PATH_FILES + "wrapper.png"));
    put(FILE_USED,       new Image(ICON_PATH_FILES + "enabled.png"));
    put(FILE_NOT_USED,   new Image(ICON_PATH_FILES + "disabled.png"));
    put(IS_FOLDER,       new Image(ICON_PATH_FILES + "folder.png"));
  }};

  /** Used to visually distinguish modified files with CSS. */
  private final PseudoClass modifiedFileClass
          = PseudoClass.getPseudoClass("modifiedFile");

  /** Used to listen to modification changes. */
  private final ChangeListener<Boolean> modificationListener = (o, ov, nv)
    -> pseudoClassStateChanged(modifiedFileClass, nv);

  /** Used to listen to usage state changes. */
  private final ChangeListener<Number> usageStateListener = ((o, ov, nv)
    -> this.setGraphic(new ImageView(ICONS_FILES.get(nv.intValue()))));

  @Override
  protected void updateItem(RudiPath rudiPath, boolean empty) {

    /* Remove old listener of the cell */
    RudiPath oldItem = getItem();
    if (oldItem != null) {
      oldItem._modifiedProperty().removeListener(modificationListener);
      oldItem._usedProperty().removeListener(usageStateListener);
    }

    super.updateItem(rudiPath, empty);

    if (empty || rudiPath == null) {

      setText(null);
      setGraphic(null);

      pseudoClassStateChanged(modifiedFileClass, false);

    } else {

      /* Visually indicate out of sync files. */
      pseudoClassStateChanged(modifiedFileClass,
              rudiPath._modifiedProperty().getValue());
      rudiPath._modifiedProperty().addListener(modificationListener);

      /* Set label of TreeItem. */
      setText(rudiPath.toString());

      /* Define icon and listener for icon. */
      setGraphic(new ImageView(ICONS_FILES.get(
              rudiPath._usedProperty().getValue())));
      rudiPath._usedProperty().addListener(usageStateListener);

    }
  }
}

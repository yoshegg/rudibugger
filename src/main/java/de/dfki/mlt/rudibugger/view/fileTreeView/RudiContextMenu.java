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

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudimant.common.ErrorInfo;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

/**
 * This is the context menu appearing when making a right click on a file /
 * folder in the fileTreeView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiContextMenu extends ContextMenu {

  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** The associated file. */
  private final RudiPath _item;

  /** TODO */
  private final DataModel _model;


  /* ***************************************************************************
   * CONSTRUCTOR
   * **************************************************************************/

  /**
   * An <code>ImportContextMenu</code> should appear when a context menu was
   * requested by clicking on a file.
   *
   * @param ri
   */
  public RudiContextMenu(RudiPath ri, DataModel model) {
    super();
    _item = ri;
    _model = model;
    initializeMenuItems();
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /**
   * Extracts the ErrorInfos information and adds it as <code>MenuItem</code>.
   */
  private void treatErrorInfo(ErrorInfo e) {
    String msg = e.getType().toString() + ": "
        + (e.getLocation().getBegin().getLine() + 1) + ":"
        + e.getLocation().getBegin().getColumn() + ": " + e.getMessage();
    Label label = new Label(msg);
    CustomMenuItem errorItem = new CustomMenuItem(label);
    Tooltip t = new Tooltip(e.getMessage());
    Tooltip.install(label, t);
    errorItem.setOnAction(f -> {
      _model.getEditor().loadFileAtLine(_item.getPath().toAbsolutePath(),
          e.getLocation().getBegin().getLine() + 1);
    });
    this.getItems().add(errorItem);
  }

  /** Initializes <code>MenuItem</code>s. */
  private void initializeMenuItems() {

    /* Set "open" MenuItem */
    CustomMenuItem openFile = new CustomMenuItem(new Label("Open "
            + _item.getPath().toAbsolutePath().getFileName().toString()));
    openFile.setOnAction((ActionEvent e) -> {
       _model.getEditor().loadFile(_item.getPath().toAbsolutePath());
    });
    this.getItems().add(openFile);

    /* If there are errors, create a separator first and then add errors */
    if (_item.getImportInfo() != null
            && ! _item.getImportInfo().getErrors().isEmpty()) {
      SeparatorMenuItem sep = new SeparatorMenuItem();
      this.getItems().add(sep);
      _item.getImportInfo().getErrors().forEach((e) -> treatErrorInfo(e));
    }

  }

}

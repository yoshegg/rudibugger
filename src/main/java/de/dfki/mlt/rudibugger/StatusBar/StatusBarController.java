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

package de.dfki.mlt.rudibugger.StatusBar;

import de.dfki.mlt.rudibugger.DataModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This <code>StatusBarController</code> class' purpose is to link the
 * <code>DataModel</code> with the statusbar of rudibugger.
 *
 * Its purpose is to
 *
 *  - reflect changes to the .rudi files to an icon,
 *  - reflect the outcome of a compilation attempt to an icon,
 *  - show status messages on the statusBar.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class StatusBarController {

  /** Logger. */
  static Logger log = LoggerFactory.getLogger("rudiLog");

  /** Current <code>DataModel</code> of rudibugger. */
  private DataModel _model;

  /**
   * Links model to controller and initializes listeners.
   *
   * @param model current <code>DataModel</code>
   */
  public void initModel(DataModel model) {
    if (this._model != null) {
      throw new IllegalStateException("Model can only be initialized once.");
    }
    _model = model;

    /* Link the model's statusBar text property with the controller */
    statusBarText.textProperty().bindBidirectional(model.statusBarTextProperty());

    /* Initialize the sync state indicator in the lower left. */
    SyncIndicator syncIndicator = new SyncIndicator(_syncIndicator, this);
    syncIndicator.linkListenerToProperty(
            _model.rudiHierarchy._modificationsAfterCompilationProperty());

    /* Initialize the compilation state indicator in the lower left. */
    CompileIndicator compileIndicator
      = new CompileIndicator(_compileIndicator, this);
    compileIndicator.linkListenerToProperty(_model._compilationStateProperty());
    compileIndicator.defineContextMenu();
  }

  /**
   * @param text text to display on the statusBar
   */
  protected void setStatusBarText(String text) {
    statusBarText.setText(text);
  }

  /**
   * @return current <code>DataModel</code>
   */
  protected DataModel getModel() { return _model; }

  /** StatusBar's label. */
  @FXML
  private Label statusBarText;

  /** Icon for sync status of .rudi and .java code. */
  @FXML
  private ImageView _syncIndicator;

  /** Icon for outcome of last compilation attempt. */
  @FXML
  private ImageView _compileIndicator;

}

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
package de.dfki.mlt.rudibugger.Controller.MenuBar;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.DataModel;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *This class manages the look and feel of the connection button.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ConnectionButtonManager {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("connectButtonMan");

  /** The <code>DataModel</code>. */
  private final DataModel _model;


  /*****************************************************************************
   * FIELDS
   ****************************************************************************/

  /** Represents the toolbar containing the compile button(s). */
  private ToolBar toolBar;

  /** Represents the connection button. */
  private Button vondaConnectionButton;


  /*****************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   ****************************************************************************/

  /** Initializes an instance of this class. */
  private ConnectionButtonManager(DataModel model) {
    _model = model;
  }

  /**
   * Initializes a new <code>ConnectionButtonManager</code> and returns it.
   *
   * @param model
   *        The current <code>DataModel</code>
   * @param button
   *        The connection button
   * @param toolBar
   *        The ToolBar containing the connection button
   * @Return An instance of this class.
   */
  public static ConnectionButtonManager init(DataModel model, Button button,
          ToolBar toolBar) {
    ConnectionButtonManager cbm = new ConnectionButtonManager(model);
    cbm.vondaConnectionButton = button;
    cbm.toolBar = toolBar;
    return cbm;
  };

  /** Manages the look (e.g. text) of the connect button. */
  protected void manageLookOfVondaConnectionButton() {

    Button button = vondaConnectionButton;

    if (_model.isProjectLoadedProperty().get() == PROJECT_CLOSED) {
      button.setText("No project");
      button.setOnMouseEntered(e -> button.setText(null));
      button.setOnMouseExited(e -> button.setText(null));
      button.setDisable(true);

    } else switch(_model.getCurrentProject().vonda.connectedProperty().get()) {
      case CONNECTED_TO_VONDA:
        button.setText("Connected");
        button.setOnMouseEntered(e -> button.setText("Disconnect"));
        button.setOnMouseExited(e -> button.setText("Connected"));
        button.setDisable(false);
        break;
      case ESTABLISHING_CONNECTION:
        button.setText("Connecting");
        button.setOnMouseEntered(e -> button.setText("Disconnect"));
        button.setOnMouseExited(e -> button.setText("Connecting"));
        button.setDisable(false);
        break;
      case DISCONNECTED_FROM_VONDA:
        button.setText("Disconnected");
        button.setOnMouseEntered(e -> button.setText("Connect"));
        button.setOnMouseExited(e -> button.setText("Disconnected"));
        button.setDisable(false);
      }
  }
}

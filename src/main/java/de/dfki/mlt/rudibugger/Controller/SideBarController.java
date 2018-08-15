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

package de.dfki.mlt.rudibugger.Controller;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.RuleTreeView.RuleTreeViewState;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller manages the left part of the rudibugger window: <br>
 *   - the TreeView showing <code>.rudi</code> files, <br>
 *   - the TreeView showing Imports and Rules, and <br>
 *   - some buttons that manipulate these <code>TreeView</code>s. <br>
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SideBarController {

  static Logger log = LoggerFactory.getLogger("sideBarController");

  /** TODO */
  private DataModel _model;


  /* ***************************************************************************
   * GUI ELEMENTS
   * **************************************************************************/

  /** Represents the root splitPane. */
  @FXML
  private SplitPane sidebarSplitPane;

  /*****************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   ****************************************************************************/

  /**
   * Connects this controller to the DataModel and initializes it by defining
   * different listeners and cell factories.
   *
   * @param model
   *        The current DataModel
   */
  public void init(DataModel model) {
    _model = model;

//
//      /* Listen to request for saving ruleLoggingState */
//      _model.getLoadedProject().getRuleModel().getRuleModelState()
//              .saveRequestProperty().addListener((o1, ov1, nv1) -> {
//        if (nv1 != null) {
//          log.debug("Requested to save ruleLoggingState.");
//          RuleTreeViewState rms = _model.getLoadedProject().getRuleModel()
//                  .getRuleModelState();
//          rms.retrieveStateOf(ruleTreeView);
//          rms.saveState(nv1);
//          rms.resetSaveRequestProperty();
//        }
//      });
//
//
//    /* Listen to request for loading ruleLoggingState */
//    _model.ruleModelState.loadRequestProperty().addListener((o, ov, nv) -> {
//      if (nv == null) return;
//      _model.ruleModelState.setStateOf(ruleTreeView);
//      _model.ruleModelState.resetLoadRequestProperty();
//    });

  }



  /*****************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS AND PROPERTIES
   ****************************************************************************/

  /** @return The SplitPane separating rule- and fileTreeView. */
  public SplitPane getSidebarSplitPane() { return sidebarSplitPane; }


}

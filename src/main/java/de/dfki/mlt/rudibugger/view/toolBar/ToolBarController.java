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
package de.dfki.mlt.rudibugger.view.toolBar;

import static de.dfki.mlt.rudibugger.Constants.CONNECTED_TO_VONDA;
import static de.dfki.mlt.rudibugger.Constants.DISCONNECTED_FROM_VONDA;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.HelperWindows;
import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudibugger.project.VondaCompiler;
import de.dfki.mlt.rudibugger.project.VondaRuntimeConnection;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ToolBarController {

  static Logger log = LoggerFactory.getLogger("ToolBarController");

  private DataModel _model;


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** Manages the compile button. */
  private CompileButtonController _compileButtonController;


  /* ***************************************************************************
   * GUI ELEMENTS
   * **************************************************************************/

  /** Represents the toolbar containing buttons. */
  @FXML
  private ToolBar toolBar;


  /** Represents the compile button. */
  @FXML
  private Button compileButton;

  /**
   * Represents the con-/disconnect button also monitoring the connection state.
   */
  @FXML
  private ConnectionButton vondaConnectionButton;


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  public void init(DataModel model) {
    _model = model;

    _compileButtonController = CompileButtonController.init(compileButton,
            toolBar);
    _model.loadedProjectProperty().addListener(ProjectListener);
  }

  private final ChangeListener<Project> ProjectListener = ((o, oldP, newP) -> {
    if (newP != null) {
      listenForCompilerChanges(newP.compiler);
        listenForRuleModelChanges(newP);
        if (newP.getRuleModel() == null) {
          vondaConnectionButton.update(newP.getRuleModel(),
            DISCONNECTED_FROM_VONDA);
        } else {
          vondaConnectionButton.update(newP.getRuleModel(),
            newP.vonda.getConnectionState());
        }
        _compileButtonController.linkToCompiler(newP.compiler);
      } else {
        _compileButtonController.disableCompileButton();
        vondaConnectionButton.reset();
      }
    }
  );


  private void listenForCompilerChanges(VondaCompiler compiler) {
    // listen to ObservableList in compiler containing all compile commands
  }

  private void listenForConnetionChanges(Project project) {
    project.vonda.connectedProperty().addListener((o, ov, nv) -> {
      vondaConnectionButton
        .update(project.getRuleModel(), project.vonda.getConnectionState());
      if (nv.intValue() == CONNECTED_TO_VONDA) {
        HelperWindows.showRuleLoggingWindow(_model.mainStage,
          _model.getLoadedProject(), _model.getEditor(), _model.globalConf);
      } else if (nv.intValue() == DISCONNECTED_FROM_VONDA) {
        HelperWindows.closeRuleLoggingWindow();
      }
    });
  }

  private void listenForRuleModelChanges(Project project) {
    project.ruleModelProperty().addListener((o, ov, ruleModel) -> {
      vondaConnectionButton.update(ruleModel,
          project.vonda.getConnectionState());
      listenForConnetionChanges(project);
    });
  }


  /* ***************************************************************************
   * LISTENERS
   * **************************************************************************/



  /* ***************************************************************************
   * GUI METHODS
   * **************************************************************************/

  /** Establishes a connection to the VOnDA server or disconnects from it. */
  @FXML
  private void toggleVondaConnectionState(ActionEvent event) {
    VondaRuntimeConnection vonda = _model.getLoadedProject().vonda;
    if (vonda.getConnectionState() == DISCONNECTED_FROM_VONDA)
      vonda.connect(_model.getLoadedProject().getVondaPort());
    else
      vonda.closeConnection();
    vondaConnectionButton.update(_model.getLoadedProject().getRuleModel(),
      vonda.getConnectionState());

  }


}

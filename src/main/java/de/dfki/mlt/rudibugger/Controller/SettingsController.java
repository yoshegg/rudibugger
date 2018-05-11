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

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */

import de.dfki.mlt.rudibugger.DataModel;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SettingsController extends Controller {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("settingsCont.");


  /*****************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   ****************************************************************************/

  /**
   * Initializes this controller.
   *
   * @param model The current <code>DataModel</code>
   */
  public void init(DataModel model) {
    linkModel(model);
    definePreSelections();
    defineListeners();
  }

  /** Reflects the current configuration to the openend settings window. */
  private void definePreSelections() {

    /* define userData */
    rudibuggerEditor.setUserData("rudibugger");
    emacsEditor.setUserData("emacs");
    customEditor.setUserData("custom");

    /* preselect current editor */
    for (Toggle x : editorSetting.getToggles()) {
      if (((RadioButton) x).getUserData().equals(_model.globalConf.getEditor()))
        x.setSelected(true);
    }

    timeStampIndexCheckBox.setSelected(
      _model.globalConf.timeStampIndexProperty().get());
    autoConnectCheckBox.setSelected(
      _model.globalConf.getAutomaticallyConnectToVonda());
    customFileEditor.setText((String) _model.globalConf.getOpenFileWith());
    customRuleEditor.setText((String) _model.globalConf.getOpenRuleWith());
  }

  /** Defines listeners. */
  private void defineListeners() {
    editorSetting.selectedToggleProperty().addListener((cl, ot, nt) -> {
      String current = (String) nt.getUserData();
      switch (current) {
        case "rudibugger":
          _model.globalConf.setSetting("editor", "rudibugger");
          _model.emacs.close(true);
          customTextFields.setDisable(true);
          break;
        case "emacs":
          _model.globalConf.setSetting("editor", "emacs");
          customTextFields.setDisable(true);
          break;
        case "custom":
          _model.globalConf.setSetting("editor", "custom");
          _model.emacs.close(true);
          customTextFields.setDisable(false);
          break;
      }
    });
    customFileEditor.textProperty().addListener((ob, ov, nv) ->
      _model.globalConf.setSetting("openFileWith", nv));
    customRuleEditor.textProperty().addListener((ob, ov, nv) ->
      _model.globalConf.setSetting("openRuleWith", nv));
    timeStampIndexCheckBox.selectedProperty().addListener((cl, ov, nv) ->
      _model.globalConf.setSetting("timeStampIndex", nv));
    autoConnectCheckBox.selectedProperty().addListener((cl, ov, nv) ->
      _model.globalConf.setSetting("automaticallyConnectToVonda", nv));
  }


  /*****************************************************************************
   * GUI ELEMENTS
   ****************************************************************************/

  /** Groups all RadioButtons used to select the default editor. */
  @FXML
  private ToggleGroup editorSetting;

  /** Should be selected when wanting to use rudibugger as editor. */
  @FXML
  private RadioButton rudibuggerEditor;

  /** Should be selected when wanting to use emacs as editor. */
  @FXML
  private RadioButton emacsEditor;

  /** Should be selected when wanting to use a custom editor. */
  @FXML
  private RadioButton customEditor;

  /** Contains the <code>TextFields</code> to specify a custom editor. */
  @FXML
  private VBox customTextFields;

  /** Represents a <code>TextField</code> specifying how to open a file with a
   * custom editor. */
  @FXML
  private TextField customFileEditor;

  /**
   * Represents a <code>TextField</code> specifying how to open a rule with a
   * custom editor.
   */
  @FXML
  private TextField customRuleEditor;

  /** Should be selected if a timestamp index should be shown in the results. */
  @FXML
  private CheckBox timeStampIndexCheckBox;

  /**
   * Should be selected if the connection to VOnDA should be established when
   * opening a project.
   */
  @FXML
  private CheckBox autoConnectCheckBox;
}

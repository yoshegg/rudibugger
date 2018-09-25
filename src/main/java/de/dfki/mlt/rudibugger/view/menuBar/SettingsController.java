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

package de.dfki.mlt.rudibugger.view.menuBar;

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */

import de.dfki.mlt.rudibugger.GlobalConfiguration;
import de.dfki.mlt.rudibugger.editor.EmacsConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SettingsController {

  /** The logger. */
  public static Logger log = LoggerFactory.getLogger("settingsCont.");

  private GlobalConfiguration _globalConf;


  /* ***************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   * **************************************************************************/

  /**
   * Initializes this controller.
   *
   * @param model The current <code>DataModel</code>
   */
  public void init(GlobalConfiguration globalConf) {
    _globalConf = globalConf;
    definePreSelections();
    defineListeners();
  }

  /** Reflects the current configuration to the opened settings window. */
  private void definePreSelections() {

    /* define userData */
    rudibuggerEditor.setUserData("rudibugger");
    emacsEditor.setUserData("emacs");
    customEditor.setUserData("custom");

    /* preselect current editor */
    for (Toggle x : editorSetting.getToggles()) {
      if (((RadioButton) x).getUserData().equals(_globalConf.getEditor()))
        x.setSelected(true);
    }

    timeStampIndexCheckBox.setSelected(
            _globalConf.timeStampIndexProperty().get());
    autoConnectCheckBox.setSelected(
      _globalConf.getAutomaticallyConnectToVonda());
    customFileEditor.setText((String) _globalConf.getOpenFileWith());
    customRuleEditor.setText((String) _globalConf.getOpenRuleWith());

    errorInfoInRuleTreeViewContextMenu.setSelected(
      _globalConf.showErrorInfoInRuleTreeViewContextMenu());
  }

  /** Defines listeners. */
  private void defineListeners() {
    editorSetting.selectedToggleProperty().addListener((cl, ot, nt) -> {
      String current = (String) nt.getUserData();
      switch (current) {
        case "rudibugger":
          _globalConf.setSetting("editor", "rudibugger");
          customTextFields.setDisable(true);
          break;
        case "emacs":
          _globalConf.setSetting("editor", "emacs");
          customTextFields.setDisable(true);
          break;
        case "custom":
          _globalConf.setSetting("editor", "custom");
          customTextFields.setDisable(false);
          break;
      }
    });
    customFileEditor.textProperty().addListener((ob, ov, nv) ->
      _globalConf.setSetting("openFileWith", nv));
    customRuleEditor.textProperty().addListener((ob, ov, nv) ->
      _globalConf.setSetting("openRuleWith", nv));
    timeStampIndexCheckBox.selectedProperty().addListener((cl, ov, nv) ->
      _globalConf.setSetting("timeStampIndex", nv));
    autoConnectCheckBox.selectedProperty().addListener((cl, ov, nv) ->
      _globalConf.setSetting("automaticallyConnectToVonda", nv));
    errorInfoInRuleTreeViewContextMenu.selectedProperty()
            .addListener((o, ov, nv) -> _globalConf
            .setSetting("showErrorInfoInRuleTreeViewContextMenu", nv));
  }


  /* ***************************************************************************
   * GUI ELEMENTS
   * **************************************************************************/

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

  /**
   * Represents a <code>TextField</code> specifying how to open a file with a
   * custom editor.
   */
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

  /**
   * Defines if the context menu in the ruleTreeView should contain a link to
   * occurred warnings and errors during compilation.
   */
  @FXML
  private CheckBox errorInfoInRuleTreeViewContextMenu;

}

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
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SettingsController {

  /** the logger of the SideBarController */
  static Logger log = LoggerFactory.getLogger("settingsController");

  /** the DataModel */
  private DataModel _model;

  /** the stage */
  private Stage _settingsStage;

  public void setDialogStage(Stage dialogStage) {
    this._settingsStage = dialogStage;
  }

  /**
   * Initializes the controller class.
   */
  public void initModel(DataModel model) {
    if (this._model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    this._model = model;

    /* define userData */
    rudibuggerEditor.setUserData("rudibugger");
    emacsEditor.setUserData("emacs");
    customEditor.setUserData("custom");

    /* define custom commands (if any) */
    customFileEditor.setText((String) _model.globalConf.getOpenFileWith());
    customRuleEditor.setText((String) _model.globalConf.getOpenRuleWith());

    /* set the listeners */
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
    customFileEditor.textProperty().addListener((ob, ov, nv) -> {
      _model.globalConf.setSetting("openFileWith", nv);
    });
    customRuleEditor.textProperty().addListener((ob, ov, nv) -> {
      _model.globalConf.setSetting("openRuleWith", nv);
    });

    /* preselect current editor */
    for (Toggle x : editorSetting.getToggles()) {
      if (((RadioButton) x).getUserData()
              .equals(_model.globalConf.getEditor())) {
        x.setSelected(true);
      }
    }

    /* TIMESTAMP CHECKBOX SETTINGS */

    /* preselect timeStampIndexCheckBox */
    timeStampIndexCheckBox.setSelected(
      _model.globalConf.timeStampIndexProperty().get()
    );

    /* listener */
    timeStampIndexCheckBox.selectedProperty().addListener((cl, ov, nv) -> {
      _model.globalConf.setSetting("timeStampIndex", nv);
    });

  }

  /*****************************************************************************
   * The different GUI elements *
   ****************************************************************************/

  @FXML
  private ToggleGroup editorSetting;

  @FXML
  private RadioButton rudibuggerEditor;

  @FXML
  private RadioButton emacsEditor;

  @FXML
  private RadioButton customEditor;

  @FXML
  private VBox customTextFields;

  @FXML
  private TextField customFileEditor;

  @FXML
  private TextField customRuleEditor;

  @FXML
  private CheckBox timeStampIndexCheckBox;
}

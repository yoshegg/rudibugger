package de.dfki.mlt.rudibugger.Controller;

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */

import de.dfki.mlt.rudibugger.DataModel;
import javafx.fxml.FXML;
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
    customFileEditor.setText(_model._globalConfigs.get("openFileWith"));
    customRuleEditor.setText(_model._globalConfigs.get("openRuleWith"));

    /* set the listeners */
    editorSetting.selectedToggleProperty().addListener((cl, ot, nt) -> {
      String current = (String) nt.getUserData();
      switch (current) {
        case "rudibugger":
          _model._globalConfigs.put("editor", "rudibugger");
          customTextFields.setDisable(true);
          break;
        case "emacs":
          _model._globalConfigs.put("editor", "emacs");
          customTextFields.setDisable(true);
          break;
        case "custom":
          _model._globalConfigs.put("editor", "custom");
          customTextFields.setDisable(false);
          break;
      }
    });
    customFileEditor.textProperty().addListener((ob, ov, nv) -> {
      _model._globalConfigs.put("openFileWith", nv);
    });
    customRuleEditor.textProperty().addListener((ob, ov, nv) -> {
      _model._globalConfigs.put("openRuleWith", nv);
    });

    /* select current editor */
    for (Toggle x : editorSetting.getToggles()) {
      if (((RadioButton) x).getUserData()
              .equals(_model._globalConfigs.get("editor"))) {
        x.setSelected(true);
      }
    }

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
}

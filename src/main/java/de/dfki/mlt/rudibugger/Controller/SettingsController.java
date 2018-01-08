package de.dfki.mlt.rudibugger.Controller;

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */

import de.dfki.mlt.rudibugger.DataModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeView;
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

    /* define editor selection logic */
    // TODO: Selection check is ugly
    ToggleGroup editorGroup = new ToggleGroup();
    rudibuggerEditor.setUserData("rudibugger");
    rudibuggerEditor.setToggleGroup(editorGroup);
    if (rudibuggerEditor.getUserData().equals(_model._globalConfigs.get("editor"))) {
      rudibuggerEditor.setSelected(true);
    }
    emacsEditor.setUserData("emacs");
    emacsEditor.setToggleGroup(editorGroup);
    if (emacsEditor.getUserData().equals(_model._globalConfigs.get("editor"))) {
      emacsEditor.setSelected(true);
    }
    customEditor.setUserData("custom");
    customEditor.setToggleGroup(editorGroup);
    if (customEditor.getUserData().equals(_model._globalConfigs.get("editor"))) {
      customEditor.setSelected(true);
    }


    /* set the listener */
    editorGroup.selectedToggleProperty().addListener((cl, ot, nt) -> {
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
          // TODO
          break;
      }

    });

  }

  /*****************************************************************************
   * The different GUI elements *
   ****************************************************************************/

  @FXML
  private RadioButton rudibuggerEditor;

  @FXML
  private RadioButton emacsEditor;

  @FXML
  private RadioButton customEditor;

  @FXML
  private VBox customTextFields;
}

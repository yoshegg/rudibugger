/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.Controller;

import static de.dfki.rudibugger.Constants.*;

import de.dfki.rudibugger.HelperWindows;
import de.dfki.rudibugger.MainApp;
import de.dfki.rudibugger.DataModel;
import de.dfki.rudibugger.TabManagement.RudiTab;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MenuController {

  /** the logger of the MenuController */
  static Logger log = Logger.getLogger("GUIlog");

  /** the DataModel */
  private DataModel _model;

  /** This function connects this controller to the DataModel
   *
   * @param model
   */
  public void initModel(DataModel model) {
    if (this._model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    _model = model;

    _model.compileFileProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue o, Object oldVal,
              Object newVal) {
        if (newVal != null) {
          log.debug("As a compile file has been found, the button was enabled.");
          compileButton.setDisable(false);
        } else {
          compileButton.setDisable(true);
        }
      }
    });

    _model.projectStatusProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue o, Object oldVal, Object newVal) {
        if ((int) newVal == PROJECT_OPEN) {
          log.debug("Project open: enable GUI-elements.");
          closeProjectItem.setDisable(false);
        } else if ((int) newVal == PROJECT_CLOSED) {
          log.debug("Project closed: disable GUI-elements.");
          closeProjectItem.setDisable(true);
        }
      }
    });

    /* this Listener enables saving depending on the selected tab */
    _model.selectedTabProperty().addListener((o, oldVal, newVal) -> {

      /* no tab is opened */
      if (newVal == null) {
        saveItem.setDisable(true);
        saveAsItem.setDisable(true);

      /* one known tab is selected and can be saved */
      } else if (((RudiTab) newVal).isKnown()) {

        /* wait until the tab content has been modified */
        newVal.hasBeenModifiedProperty().addListener((o2, oldVal2, newVal2) -> {
          if (newVal2) {
            saveItem.setDisable(false);
          } else {
            saveItem.setDisable(true);
          }
        });
        
        saveAsItem.setDisable(false);

      /* a newly created file can only be saved as */
      } else if (! ((RudiTab) newVal).isKnown()) {
        saveItem.setDisable(true);
        saveAsItem.setDisable(false);
      }
    });
  }


  /******************************
   * The different GUI elements *
   ******************************/

  /* the compile button */
  @FXML
  private Button compileButton;

  /* the run button */
  @FXML
  private Button runButton;

  /**************************************
   * Menu items actions (from menu bar) *
   **************************************/

  /********* File *********/


  /** MenuItem "New Project..." */
  @FXML
  private MenuItem newProjectItem;

  /** Action "New Project..." */
  @FXML
  private void newProjectAction(ActionEvent event) {
    _model.createNewProject();
  }


  /** MenuItem "New rudi File..." */
  @FXML
  private MenuItem newRudiFileItem;

  /** Action "New rudi File..." */
  @FXML
  private void newRudiFileAction(ActionEvent event)
          throws FileNotFoundException {
    log.debug("method missing");
  }


  /** MenuItem "Open Project..." */
  @FXML
  private MenuItem openProjectItem;

  /** Action "Open Project..." */
  @FXML
  private void openProjectAction(ActionEvent event)
          throws FileNotFoundException, IOException, IllegalAccessException {
//    if (model.projectX != null) {
//      if (HelperWindows.overwriteProjectCheck(model.projectX) != 0) return;
//    }
//    if (model.openProjectYml(model.fileTreeView, model.ruleTreeView, model.tabPaneBack)) {
//      // enable buttons of respective files have been found
//      if (model.projectX.getRunFile() != null) {
//        runButton.setDisable(false);
//        log.debug("Enabled run button.");
//      }
//      if (model.projectX.getCompileFile() != null) {
//        compileButton.setDisable(false);
//        log.debug("Enabled compile button.");
//      }
//    }

    Path ymlFile = HelperWindows.openYmlProjectFile(_model.stageX);
    _model.initProject(ymlFile);
  }


  /** Menu "Open Recent Project" */
  @FXML
  private Menu openRecentProjectMenu;


  /** MenuItem "Close Project" */
  @FXML
  private MenuItem closeProjectItem;

  /** Action "Close Project" */
  @FXML
  private void closeProjectAction(ActionEvent event)
          throws FileNotFoundException {
    _model.closeProject();
  }


  /** Menu "Load logging state" */
  @FXML
  private Menu loadLoggingStateMenu;


  /** MenuItem "Save logging state..." */
  @FXML
  private MenuItem saveLoggingStateItem;

  /** Action "Save logging state" */
  @FXML
  private void saveLoggingStateAction(ActionEvent event) {

  }


  /** MenuItem "Save" */
  @FXML
  private MenuItem saveItem;

  /** Action "Save" */
  @FXML
  private void saveAction(ActionEvent event) {
    _model.updateFile();
  }


  /** MenuItem "Save as..." */
  @FXML
  private MenuItem saveAsItem;

  /** Action "Save as..." */
  @FXML
  private void saveAsAction(ActionEvent event) {
    _model.saveFileAs();
  }


  /** MenuItem "Save all" */
  @FXML
  private MenuItem saveAllItem;

  /** Action "Save all" */
  @FXML
  private void saveAllAction(ActionEvent event) {

  }


  /** MenuItem "Exit" */
  @FXML
  private MenuItem exitItem;

  /** Action "Exit" */
  @FXML
  private void exitAction(ActionEvent event) {
    MainApp.exitRudibugger();
  }



  /******************
   * Button actions *
   ******************/

  /* Clicking the compile button */
  @FXML
  private void startCompile(ActionEvent event) throws IOException, InterruptedException {
    _model.startCompile();
  }

  /* Clicking the run button */
  @FXML
  private void startRun(ActionEvent event) {
    log.warn("\"Run\" is not implemented yet.");
  }



  /* for testing purposes: open dipal */
  @FXML
  private void openDipal(ActionEvent event)
          throws FileNotFoundException, IOException, IllegalAccessException {
    _model.initProject(new File("/home/christophe/projects/dialoguemanager.dipal/dipal.yml").toPath());
  }
}

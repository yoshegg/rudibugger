/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.Controller;

import de.dfki.rudibugger.HelperWindows;
import de.dfki.rudibugger.MainApp;
import de.dfki.rudibugger.mvc.DataModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
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

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /* the model */
  private DataModel model;

  public void initModel(DataModel model) {
    if (this.model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    this.model = model;
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
    model.createNewProject();
  }


  /** MenuItem "New rudi File..." */
  @FXML
  private MenuItem newRudiFileItem;

  /** Action "New rudi File..." */
  @FXML
  private void newRudiFileAction(ActionEvent event)
          throws FileNotFoundException {
    model.newRudiFile();
  }


  /** MenuItem "Open Project..." */
  @FXML
  private MenuItem openProjectItem;

  /** Action "Open Project..." */
  @FXML
  private void openProjectAction(ActionEvent event)
          throws FileNotFoundException, IOException {
//    if (model.projectX != null) {
//      if (HelperWindows.overwriteProjectCheck(model.projectX) != 0) return;
//    }
//    if (model.openProjectYml(folderTreeView, ruleTreeView, tabPaneBack)) {
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
//    folderTreeView.setRoot(null);
//    String name = model.projectX.getProjectName();
//    log.debug("Closed project [" + name + "].");
//    runButton.setDisable(true);
//    compileButton.setDisable(true);
//    log.debug("Disabled compile and run buttons.");
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

  }


  /** MenuItem "Save as..." */
  @FXML
  private MenuItem saveAsItem;

  /** Action "Save as..." */
  @FXML
  private void saveAsAction(ActionEvent event) {

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
//    model.startCompile(ruleTreeView);
  }

  /* Clicking the run button */
  @FXML
  private void startRun(ActionEvent event) {
    log.warn("\"Run\" is not implemented yet.");
  }



  /* for testing purposes: open dipal */
  @FXML
  private void openDipal(ActionEvent event)
          throws FileNotFoundException, IOException {
//    Path ymlFile = new File("/home/christophe/projects/dialoguemanager.dipal/dipal.yml").toPath();
//    if (model.projectX != null) {
//      if (HelperWindows.overwriteProjectCheck(model.projectX) != 0) return;
//    }
//
//    if (model.processProjectYml(ymlFile, folderTreeView, ruleTreeView,
//            tabPaneBack)) {
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
  }
}

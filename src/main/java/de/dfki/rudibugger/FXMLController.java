/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger;

import de.dfki.rudibugger.project.RudiFileTreeItem;
import de.dfki.rudibugger.tabs.RudiHBox;
import de.dfki.rudibugger.tabs.RudiTab;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class FXMLController implements Initializable {

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /* the model */
  private final Model model;


  /*********************************************
   * The constructor and its initialize method *
   *********************************************/

  /* the constructor binds the model to the controller */
  public FXMLController(Model model) {
    this.model = model;
  }

  /* this method is automatically called */
  @Override
  public void initialize(URL url, ResourceBundle rb) {

    /* initialise the RudiHBox for the RudiTabPane(s) */
    tabPaneBack = new RudiHBox();
    tabPaneBack.fitToParentAnchorPane();
    tabAnchorPane.getChildren().add(tabPaneBack);

    /* what should happen when a .rudi file is double clicked */
    folderTreeView.setOnMouseClicked((MouseEvent mouseEvent) -> {
      if (mouseEvent.getClickCount() == 2) {
        Object test = folderTreeView.getSelectionModel().getSelectedItem();
        if (test instanceof RudiFileTreeItem) {
          RudiFileTreeItem item = (RudiFileTreeItem) test;
          RudiTab tab = tabPaneBack.getTab(item.getFile());
        }
      }
    });
  }


  /******************************
   * The different GUI elements *
   ******************************/

  /* statusbar at the bottom */
  @FXML
  private Label statusBar;

  /* the ground AnchorPane of the HBox containing the tabPane(s) */
  @FXML
  private AnchorPane tabAnchorPane;

  /* the RudiHBox on top of tabAnchorPane */
  private RudiHBox tabPaneBack;

  /* the compile button */
  @FXML
  private Button compileButton;

  /* the run button */
  @FXML
  private Button runButton;

  /* The TreeView showing the content of the rudi folder (or root folder) */
  @FXML
  private TreeView folderTreeView;

  /* The TreeView showing the different rudi rules and imports */
  @FXML
  private TreeView ruleTreeView;


  /********************************
   * Menu actions (from menu bar) *
   ********************************/

  /* File -> New empty tab */
  @FXML
  private void newEmptyTab(ActionEvent event) throws FileNotFoundException {
    model.newEmptyTab(tabPaneBack);
  }

  /* File -> Open file(s) to tabs(s)... */
  @FXML
  private void openFile(ActionEvent event) throws FileNotFoundException {
    model.openFile(tabPaneBack);
  }

  /* File -> Close file */
  @FXML
  private void closeFile(ActionEvent event) {
    log.warn("Action \"Close file\" is not implemented yet");
  }

  /* File -> Exit */
  @FXML
  private void closeApplication(ActionEvent event) {
    MainApp.exitRudibugger();
  }

  /* File -> Open project .yml file... */
  @FXML
  private void openProjectYml(ActionEvent event) throws FileNotFoundException, IOException {
    if (model.projectX != null) {
      if (!Helper.overwriteProjectCheck(model.projectX)) return;
    }
    if (model.openProjectYml(folderTreeView, ruleTreeView, tabPaneBack)) {
      // enable buttons of respective files have been found
      if (model.projectX.getRunFile() != null) {
        runButton.setDisable(false);
        log.debug("Enabled run button.");
      }
      if (model.projectX.getCompileFile() != null) {
        compileButton.setDisable(false);
        log.debug("Enabled compile button.");
      }
    }
  }

  /* File -> Open project directory... */
  @FXML
  private void openProjectDirectory(ActionEvent event) throws IOException {
    if (model.projectX != null) {
      if (Helper.overwriteProjectCheck(model.projectX)) {
        model.openProjectDirectoryChooser(folderTreeView);
      }
    } else {
      model.openProjectDirectoryChooser(folderTreeView);
    }
  }

  /* File -> Close project... */
  @FXML
  private void closeProject(ActionEvent event) {
    folderTreeView.setRoot(null);
    String name = model.projectX.getProjectName();
    log.debug("Closed project [" + name + "].");
    runButton.setDisable(true);
    compileButton.setDisable(true);
    log.debug("Disabled compile and run buttons.");
  }


  /******************
   * Button actions *
   ******************/

  /* Clicking the compile button */
  @FXML
  private void startCompile(ActionEvent event) throws IOException, InterruptedException {
    model.startCompile(ruleTreeView);
  }

  /* Clicking the run button */
  @FXML
  private void startRun(ActionEvent event) {
    log.warn("\"Run\" is not implemented yet.");
  }


  /****************
   * TESTING ONLY *
   ****************/

  /* for testing purposes: open dipal */
  @FXML
  private void openDipal(ActionEvent event)
          throws FileNotFoundException, IOException {
    Path ymlFile = new File("/home/christophe/projects/dialoguemanager.dipal/dipal.yml").toPath();
    if (model.projectX != null) {
      if (!Helper.overwriteProjectCheck(model.projectX)) return;
    }

    if (model.processOpeningProjectYml(ymlFile, folderTreeView, ruleTreeView,
            tabPaneBack)) {
      // enable buttons of respective files have been found
      if (model.projectX.getRunFile() != null) {
        runButton.setDisable(false);
        log.debug("Enabled run button.");
      }
      if (model.projectX.getCompileFile() != null) {
        compileButton.setDisable(false);
        log.debug("Enabled compile button.");
      }
    }
  }

//  /* Actually just a testing function, will be removed later */
//  @FXML
//  private void tabClicked(MouseEvent event) {
//    log.debug("tab clicked!");
//    if (event.getButton() == MouseButton.MIDDLE) {
//      log.debug("Middle mouse button clicked!");
//      Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
//      log.debug(selectedTab);
//    }
//  }
}

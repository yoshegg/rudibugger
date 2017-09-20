package de.dfki.rudibugger;

import de.dfki.rudibugger.project.Project;
import de.dfki.rudibugger.project.RudiFileTreeItem;
import de.dfki.rudibugger.tabs.RudiTab;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class FXMLController implements Initializable {

  static Logger log = Logger.getLogger("rudiLog");

  private final Model model;

  public FXMLController(Model model) {
    this.model = model;
  }

  @FXML
  private Label statusbar;

  @FXML
  private TabPane tabpanex;

  @FXML
  private Button compileButton;

  @FXML
  private Button runButton;

  /* The treeview in the upper left part of the window */
  @FXML
  private TreeView foldertreeviewx;

  /* The treeview in the lower left part of the window
   * used to set logging of rules
   */
  @FXML
  private TreeView locRuleViewx;

  /* Actually just a testing function, will be removed later */
  @FXML
  private void tabClicked(MouseEvent event) {
    log.debug("tab clicked!");

    if (event.getButton() == MouseButton.MIDDLE) {
      log.debug("Middle mouse button clicked!");
      Tab selectedTab = tabpanex.getSelectionModel().getSelectedItem();
      log.debug(selectedTab);
    }
  }

  /* File -> Exit */
  @FXML
  private void closeApplication(ActionEvent event) {
    MainApp.exitRudibugger();
  }

  /* File -> New empty tab */
  @FXML
  private void newEmptyTab(ActionEvent event) throws FileNotFoundException {
    model.newEmptyTab(tabpanex);
  }

  /* File -> Open file(s) to tabs(s)... */
  @FXML
  private void openFile(ActionEvent event) throws FileNotFoundException {
    model.openFile(tabpanex);
  }

  /* File -> Open project directory... */
  @FXML
  private void openProjectDirectory(ActionEvent event) {
    if (model.projectX != null) {
      if (Helper.overwriteProjectCheck(model.projectX)) {
        model.openProjectDirectoryChooser(foldertreeviewx);
      }
    } else {
      model.openProjectDirectoryChooser(foldertreeviewx);
    }
  }

  /* File -> Open project .yml file... */
  @FXML
  private void openProjectYml(ActionEvent event) throws FileNotFoundException {
    if (model.projectX != null) {
      if (!Helper.overwriteProjectCheck(model.projectX)) return;
    }
    if (model.openProjectYml(foldertreeviewx, locRuleViewx)) {
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


  /* File -> Close project... */
  @FXML
  private void closeProject(ActionEvent event) {
    foldertreeviewx.setRoot(null);
    String name = model.projectX.getProjectName();
    Project.clearProject();
    log.debug("Closed project [" + name + "].");
    runButton.setDisable(true);
    compileButton.setDisable(true);
    log.debug("Disabled compile and run buttons.");
  }

  @FXML
  private void startRun(ActionEvent event) {

  }

  @FXML
  private void startCompile(ActionEvent event) throws IOException, InterruptedException {
    model.startCompile(locRuleViewx);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {

    // make all tabs closable with an X button
    tabpanex.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);

    // https://stackoverflow.com/questions/17348357/how-to-trigger-event-when-double-click-on-a-tree-node
    foldertreeviewx.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
          Object test = foldertreeviewx.getSelectionModel().getSelectedItem();
          if (test instanceof RudiFileTreeItem) {
            RudiFileTreeItem item = (RudiFileTreeItem) test;
            System.out.println("Selected Text : " + item.getValue());
            RudiTab tabdata;
            try {
              tabdata = new RudiTab(tabpanex, item.getFile());
            } catch (FileNotFoundException ex) {
              log.fatal(ex);
            }
          }
        }
      }
    });
  }
}

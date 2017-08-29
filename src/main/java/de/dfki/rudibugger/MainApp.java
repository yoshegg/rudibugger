package de.dfki.rudibugger;

import de.dfki.rudibugger.folderstructure.RudiTreeItem;
import de.dfki.rudibugger.tabs.RudiTab;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MainApp extends Application {

  static Logger log = Logger.getLogger("rudiLog");

  Stage stageX;

  File projectX;

  @Override
  public void start(Stage stage) throws Exception {
    BasicConfigurator.configure();
    PropertyConfigurator.configure("src/main/resources/log4j/log4j.properties");
    stageX = stage;
    Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));

    Scene scene = new Scene(root);
    //scene.getStylesheets().add("/styles/Styles.css");

    stage.setTitle("Rudibugger (beta)");
    stage.setScene(scene);
    stage.show();
  }

  private static MainApp instance;

  public MainApp() {
    instance = this;
  }

  public static MainApp getInstance() {
    return instance;
  }

  /**
   * The main() method is ignored in correctly deployed JavaFX application.
   * main() serves only as fallback in case the application can not be launched
   * through deployment artifacts, e.g., in IDEs with limited FX support.
   * NetBeans ignores main().
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }

  public void openFile(TabPane tabpanex) throws FileNotFoundException {
    log.debug("Opening file chooser.");
    FileChooser fileChooser = new FileChooser();

    // Set extension filter
    FileChooser.ExtensionFilter extFilterRUDI = new FileChooser.ExtensionFilter("RUDI files (*.rudi)", "*.rudi", "*.RUDI");
    FileChooser.ExtensionFilter extFilterALL = new FileChooser.ExtensionFilter("All files", "*.*");
    fileChooser.getExtensionFilters().addAll(extFilterRUDI);
    fileChooser.getExtensionFilters().addAll(extFilterALL);

    fileChooser.setTitle("Open .rudi file(s)");
    List selectedFiles = fileChooser.showOpenMultipleDialog(stageX);

    // iterate over chosen files and open them in a new tab
    for (Object file : selectedFiles) {
      RudiTab tab = new RudiTab(tabpanex, (File) file);
    }
  }

  // https://stackoverflow.com/questions/35070310/javafx-representing-directories
  public void openProject(TreeView treeviewx) {
    DirectoryChooser dc = new DirectoryChooser();
    dc.setInitialDirectory(new File(System.getProperty("user.home")));
    File choice = dc.showDialog(stageX);
    if(choice == null || ! choice.isDirectory()) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setHeaderText("Could not open directory");
        alert.setContentText("The file is invalid.");

        alert.showAndWait();
    } else {
        projectX = choice;
        treeviewx.setRoot(getNodesForDirectory(choice));
        treeviewx.getRoot().setExpanded(true);
    }
  }

  /*
   * Returns a TreeItem representation of the specified directory
   */
  public TreeItem<String> getNodesForDirectory(File directory) {
    TreeItem<String> root = new TreeItem<String>(directory.getName());
    for (File f : directory.listFiles()) {
      if (f.isDirectory()) { //Then we call the function recursively
        root.getChildren().add(getNodesForDirectory(f));
      } else {
        if (f.getName().contains(".")) {
          String fileEnding = f.getName().substring(f.getName().lastIndexOf('.'));
          if (".rudi".equals(fileEnding)) {
            RudiTreeItem item = new RudiTreeItem(f.getName());
            item.setFile(f.getAbsolutePath());
            root.getChildren().add(item);
          }
        }
      }
    }
    return root;
  }

  public static void exitRudibugger() {
    Platform.exit();
  }

}

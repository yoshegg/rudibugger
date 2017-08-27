package de.dfki.rudibugger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MainApp extends Application {
  
  Stage stageX;
  
    @Override
    public void start(Stage stage) throws Exception {
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
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public void openFile() {
    FileChooser fileChooser = new FileChooser();
    
    //Set extension filter
    FileChooser.ExtensionFilter extFilterRUDI = new FileChooser.ExtensionFilter("RUDI files (*.rudi)", "*.rudi", "*.RUDI");
    FileChooser.ExtensionFilter extFilterALL = new FileChooser.ExtensionFilter("All files", "*.*");
    fileChooser.getExtensionFilters().addAll(extFilterRUDI);
    fileChooser.getExtensionFilters().addAll(extFilterALL);
    
    fileChooser.setTitle("Open .rudi file");
    List selectedFiles = fileChooser.showOpenMultipleDialog(stageX);
    System.out.println(selectedFiles);
    
    
  }
  
  public static void exitRudibugger() {
    Platform.exit();
  }
  
}

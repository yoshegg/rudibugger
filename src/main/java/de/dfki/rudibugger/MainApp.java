package de.dfki.rudibugger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

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
    FileChooser fileChooser = new FileChooser();

    // Set extension filter
    FileChooser.ExtensionFilter extFilterRUDI = new FileChooser.ExtensionFilter("RUDI files (*.rudi)", "*.rudi", "*.RUDI");
    FileChooser.ExtensionFilter extFilterALL = new FileChooser.ExtensionFilter("All files", "*.*");
    fileChooser.getExtensionFilters().addAll(extFilterRUDI);
    fileChooser.getExtensionFilters().addAll(extFilterALL);

    fileChooser.setTitle("Open .rudi file");
    List selectedFiles = fileChooser.showOpenMultipleDialog(stageX);

    // iterate over chosen files:
    for (Object file : selectedFiles) {

      // create new tab
      Tab tab = new Tab("Tab " + (tabpanex.getTabs().size() + 1));
      tabpanex.getTabs().add(tab);
      tabpanex.getSelectionModel().select(tab);

      AnchorPane content = new AnchorPane();
      CodeArea textArea = new CodeArea();
      textArea.setWrapText(true);
      textArea.setParagraphGraphicFactory(LineNumberFactory.get(textArea));

      // set TextArea to fit parent Anchor Pane
      AnchorPane.setTopAnchor(textArea, 0.0);
      AnchorPane.setRightAnchor(textArea, 0.0);
      AnchorPane.setLeftAnchor(textArea, 0.0);
      AnchorPane.setBottomAnchor(textArea, 0.0);
      //textArea.setId("test");
      content.getChildren().add(textArea);
      tab.setContent(content);

      // https://stackoverflow.com/questions/27222205/javafx-read-from-text-file-and-display-in-textarea
      Scanner s = new Scanner((File) file).useDelimiter("\n");
      while (s.hasNext()) {
        textArea.appendText(s.next() + "\n");
      }
    }
  }

  public static void exitRudibugger() {
    Platform.exit();
  }

}

package de.dfki.rudibugger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MainApp extends Application {

  static Logger log = Logger.getLogger("rudiLog");

  @Override
  public void start(Stage stage) throws Exception {

    // initialize log4j
    BasicConfigurator.configure();
    PropertyConfigurator.configure("src/main/resources/log4j/log4j.properties");
    log.info("Starting Rudibugger");

    // initialize FXMLController
    final Model model = new Model();
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(getClass().getResource("/fxml/Scene.fxml"));
    loader.setControllerFactory(new Callback<Class<?>, Object>() {
      @Override
      public Object call(Class<?> aClass) {
        return new FXMLController(model);
      }
    });

    // bind stage to field
    model.stageX = stage;

    // define GUI
    Parent root = (Parent) loader.load();
    Scene scene = new Scene(root);
    //scene.getStylesheets().add("/styles/Styles.css");
    stage.setTitle("Rudibugger (beta)");
    stage.setScene(scene);
    Image icon = new Image("file:src/main/resources/icons/baggerschaufel_titlebar_32x32.png");
    stage.getIcons().add(icon);

    // show Rudibugger
    stage.show();
    log.info("Rudibugger has been started");
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

  public static void exitRudibugger() {
    Platform.exit();
  }

}

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger;

import de.dfki.rudibugger.Controller.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MainApp extends Application {

  /* the logger */
  static Logger log = Logger.getLogger("mainLog");

  @Override
  public void start(Stage stage) throws Exception {

    /* initialize log4j */
    BasicConfigurator.configure();
    PropertyConfigurator.configure("src/main/resources/log4j/log4j.properties");
    log.info("Starting Rudibugger");

    /*
     * INITIALIZE CONTROLLERS AND FXMLs
     */

 /* create root BorderPane */
    BorderPane root = new BorderPane();

    /* initialize menuBar (top of BorderPane) */
    FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/fxml/menuBar.fxml"));
    root.setTop(menuLoader.load());
    MenuController menuController = menuLoader.getController();

    /* initialize statusBar (bottom of BorderPane) */
    FXMLLoader statusLoader = new FXMLLoader(getClass().getResource("/fxml/statusBar.fxml"));
    root.setBottom(statusLoader.load());
    StatusBarController statusBarController = statusLoader.getController();

    /* prepare SplitPane in the center of the root BorderPane */
    SplitPane centeredSplitPane = new SplitPane();
    centeredSplitPane.setOrientation(Orientation.HORIZONTAL);
    centeredSplitPane.setDividerPositions(0.30);
    root.setCenter(centeredSplitPane);

    /* initialize sideBar (left part of centeredSplitPane) */
    FXMLLoader sideBarLoader = new FXMLLoader(getClass().getResource("/fxml/sideBar.fxml"));
    centeredSplitPane.getItems().add(sideBarLoader.load());
    SideBarController sideBarController = sideBarLoader.getController();

    /* initalize editor (right part of centeredSplitPane) */
    FXMLLoader editorLoader = new FXMLLoader(getClass().getResource("/fxml/editor.fxml"));
    centeredSplitPane.getItems().add(editorLoader.load());
    EditorController editorController = editorLoader.getController();


    /*
     * INITIALIZE DATA MODEL
     */
    DataModel model = new DataModel();
    model.initialize();
    menuController.initModel(model);
    statusBarController.initModel(model);
    sideBarController.initModel(model);
    editorController.initModel(model);

    /* bind stage to field */
    model.stageX = stage;

    /* define GUI */
    Scene scene = new Scene(root);
    scene.getStylesheets().add("/styles/Styles.css");
    stage.setTitle("Rudibugger (beta)");
    stage.setScene(scene);
    Image icon = new Image("file:src/main/resources/icons/baggerschaufel_titlebar_32x32.png");
    stage.getIcons().add(icon);

    /* show Rudibugger */
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

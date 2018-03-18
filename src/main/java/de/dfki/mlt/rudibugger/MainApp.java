/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger;

import de.dfki.mlt.rudibugger.Controller.EditorController;
import de.dfki.mlt.rudibugger.Controller.MenuController;
import de.dfki.mlt.rudibugger.Controller.SideBarController;
import de.dfki.mlt.rudibugger.StatusBar.StatusBarController;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MainApp extends Application {

  /** the logger */
  static Logger log = LoggerFactory.getLogger("mainLog");

  @Override
  public void start(Stage stage) throws Exception {

    /* initialize log4j / sfl4j */
    //BasicConfigurator.configure();
    //PropertyConfigurator.configure("src/main/resources/log4j.properties");

    log.info("Starting Rudibugger");

    /***************************************************************************
     * GENERAL FIXES
     **************************************************************************/

    /* improve font rendering for RichTextFX, caused by JavaFX bug */
    /* https://github.com/FXMisc/RichTextFX/wiki/Known-Issues */
    System.setProperty("prism.lcdtext", "false");
    System.setProperty("prism.text", "t2k");


    /***************************************************************************
     * INITIALIZE CONTROLLERS AND FXMLs
     **************************************************************************/

    /* create root BorderPane */
    BorderPane root = new BorderPane();

    /* initialize menuBar (top of BorderPane) */
    FXMLLoader menuLoader = new FXMLLoader(getClass()
            .getResource("/fxml/menuBar.fxml"));
    root.setTop(menuLoader.load());
    MenuController menuController = menuLoader.getController();

    /* initialize statusBar (bottom of BorderPane) */
    FXMLLoader statusLoader = new FXMLLoader(getClass()
            .getResource("/fxml/statusBar.fxml"));
    root.setBottom(statusLoader.load());
    StatusBarController statusBarController = statusLoader.getController();

    /* prepare SplitPane in the center of the root BorderPane */
    SplitPane centeredSplitPane = new SplitPane();
    centeredSplitPane.setOrientation(Orientation.HORIZONTAL);
    centeredSplitPane.setDividerPositions(0.30);
    root.setCenter(centeredSplitPane);

    /* initialize sideBar (left part of centeredSplitPane) */
    FXMLLoader sideBarLoader = new FXMLLoader(getClass()
            .getResource("/fxml/sideBar.fxml"));
    AnchorPane sideBar = sideBarLoader.load();
    centeredSplitPane.getItems().add(sideBar);
    SideBarController sideBarController = sideBarLoader.getController();
    SplitPane.setResizableWithParent(sideBar, Boolean.FALSE);

    /* initalize editor (right part of centeredSplitPane) */
    FXMLLoader editorLoader = new FXMLLoader(getClass()
            .getResource("/fxml/editor.fxml"));
    centeredSplitPane.getItems().add(editorLoader.load());
    EditorController editorController = editorLoader.getController();


    /***************************************************************************
     * INITIALIZE DATA MODEL
     **************************************************************************/

    DataModel model = new DataModel();

    /* create a global config folder if there is none */
    if (! Files.exists(model.globalConfigPath)) {
      model.globalConfigPath.toFile().mkdirs();
      Files.createFile(model.globalConfigPath.resolve("recentProjects.yml"));
      log.info("Created global config folder (first start of rudibugger");
    }

    model.initialize();
    model.initializeGlobalKnowledge();
    model.keepGlobalKnowledgeUpToDate();

    menuController.initModel(model);
    statusBarController.initModel(model);
    sideBarController.initModel(model);
    editorController.initModel(model);


    /***************************************************************************
     * PREPARE GUI
     **************************************************************************/

    /* bind stage to field */
    model.stageX = stage;

    /* define GUI */
    Scene scene = new Scene(root);
    scene.getStylesheets().add("/styles/Styles.css");
    stage.setTitle("Rudibugger");
    stage.setScene(scene);
    Image icon = new Image("file:src/main/resources/"
            + "icons/baggerschaufel_titlebar_32x32.png");
    stage.getIcons().add(icon);

    /* show Rudibugger */
    stage.show();
    log.info("Rudibugger has been started");

    /* opening last openend project (if any) */
    if (! model._globalConfigs.get("lastOpenedProject").equals("")) {
      log.info("Opening previous project...");
      model.initProject(Paths.get(
              (String) model._globalConfigs.get("lastOpenedProject")));
    }

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

  /** method to correctly exit this JavaFX application */
  public static void exitRudibugger() {
    Platform.exit();
  }

}

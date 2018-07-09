/*
 * The Creative Commons CC-BY-NC 4.0 License
 *
 * http://creativecommons.org/licenses/by-nc/4.0/legalcode
 *
 * Creative Commons (CC) by DFKI GmbH
 *  - Bernd Kiefer <kiefer@dfki.de>
 *  - Anna Welker <anna.welker@dfki.de>
 *  - Christophe Biwer <christophe.biwer@dfki.de>
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.dfki.mlt.rudibugger;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.Controller.EditorController;
import de.dfki.mlt.rudibugger.Controller.MenuBar.MenuController;
import de.dfki.mlt.rudibugger.Controller.SideBarController;
import de.dfki.mlt.rudibugger.StatusBar.StatusBarController;
import static de.dfki.mlt.rudibugger.ViewLayout.*;
import java.nio.file.Files;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the main method to start rudibugger.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MainApp extends Application {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("mainLog");

  @Override
  public void start(Stage stage) throws Exception {

    log.info("Starting Rudibugger");

    /***************************************************************************
     * GENERAL FIXES
     **************************************************************************/

    /* Improve font rendering for RichTextFX, caused by JavaFX bug */
    /* https://github.com/FXMisc/RichTextFX/wiki/Known-Issues */
    System.setProperty("prism.lcdtext", "false");
    System.setProperty("prism.text", "t2k");


    /***************************************************************************
     * INITIALIZE CONTROLLERS AND FXMLs
     **************************************************************************/

    /* Create root BorderPane */
    BorderPane root = new BorderPane();

    /* Initialize menuBar (top of BorderPane) */
    FXMLLoader menuLoader = new FXMLLoader(getClass()
            .getResource("/fxml/menuBar.fxml"));
    root.setTop(menuLoader.load());
    MenuController menuController = menuLoader.getController();

    /* Initialize statusBar (bottom of BorderPane) */
    FXMLLoader statusLoader = new FXMLLoader(getClass()
            .getResource("/fxml/statusBar.fxml"));
    root.setBottom(statusLoader.load());
    StatusBarController statusBarController = statusLoader.getController();

    /* Prepare SplitPane in the center of the root BorderPane */
    SplitPane centeredSplitPane = new SplitPane();
    centeredSplitPane.setOrientation(Orientation.HORIZONTAL);
    centeredSplitPane.setDividerPositions(0.30);
    root.setCenter(centeredSplitPane);

    /* Initialize sideBar (left part of centeredSplitPane) */
    FXMLLoader sideBarLoader = new FXMLLoader(getClass()
            .getResource("/fxml/sideBar.fxml"));
    AnchorPane sideBar = sideBarLoader.load();
    centeredSplitPane.getItems().add(sideBar);
    SideBarController sideBarController = sideBarLoader.getController();
    SplitPane.setResizableWithParent(sideBar, Boolean.FALSE);

    /* Initalize editor (right part of centeredSplitPane) */
    FXMLLoader editorLoader = new FXMLLoader(getClass()
            .getResource("/fxml/editor.fxml"));
    centeredSplitPane.getItems().add(editorLoader.load());
    EditorController editorController = editorLoader.getController();


    /***************************************************************************
     * INITIALIZE DATA MODEL
     **************************************************************************/

    DataModel model = new DataModel();

    /* Create a global config folder if there is none */
    if (! Files.exists(GLOBAL_CONFIG_PATH)) {
      GLOBAL_CONFIG_PATH.toFile().mkdirs();
      log.info("Created global config folder (first start of rudibugger");
    }

    menuController.init(model);
    statusBarController.initModel(model);
    sideBarController.init(model);
    editorController.initModel(model);


    /***************************************************************************
     * PREPARE GUI
     **************************************************************************/

    /* Bind stage to field */
    model.stageX = stage;

    /* Define GUI */
    Scene scene = new Scene(root);
    scene.getStylesheets().add("/styles/Styles.css");
    stage.setTitle("Rudibugger");
    stage.setScene(scene);
    Image icon = new Image("file:src/main/resources/"
            + "icons/baggerschaufel_titlebar_32x32.png");
    stage.getIcons().add(icon);

    /* Link splitpanes to model.layout */
    model.layout.addSplitPane(sideBarController.getSidebarSplitPane(),
            DIVIDER_SIDEBAR);
    model.layout.addSplitPane(centeredSplitPane, DIVIDER_SIDEBAR_EDITOR);

    /* Restore the layout and set listener */
    model.layout.restoreWindowPosition();
    model.layout.setStageCloseListener();
    model.layout.restoreDividerPositions();

    /* Show Rudibugger */
    stage.show();
    log.info("Rudibugger has been started");

    /* Open last openend project (if any) */
    if (model.globalConf.getLastOpenedProject() != null) {
      log.info("Opening previous project...");
      model.init(model.globalConf.getLastOpenedProject());
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

  /** Correctly exits this JavaFX application. */
  public static void exitRudibugger() {
    Platform.exit();
  }

}

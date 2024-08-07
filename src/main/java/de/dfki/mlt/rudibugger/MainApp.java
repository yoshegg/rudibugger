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

import de.dfki.mlt.rudibugger.view.editor.RudibuggerEditorController;
import de.dfki.mlt.rudibugger.view.menuBar.MenuBarController;
import de.dfki.mlt.rudibugger.view.fileTreeView.FileTreeViewController;
import de.dfki.mlt.rudibugger.view.ruleTreeView.RuleTreeViewController;
import de.dfki.mlt.rudibugger.view.ruleTreeView.RuleTreeViewState;
import de.dfki.mlt.rudibugger.view.statusBar.StatusBarController;
import static de.dfki.mlt.rudibugger.ViewLayout.*;

import java.nio.file.Path;

import de.dfki.mlt.rudibugger.editor.RudibuggerEditor;
import de.dfki.mlt.rudibugger.view.toolBar.ToolBarController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the main method to start rudibugger.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MainApp extends Application {

  static Logger log = LoggerFactory.getLogger("mainLog");


  @Override
  public void start(Stage stage) throws Exception {

    log.info("Starting Rudibugger");

    /* *************************************************************************
     * GENERAL FIXES
     * ************************************************************************/

    /* Improve font rendering for RichTextFX, caused by JavaFX bug */
    /* https://github.com/FXMisc/RichTextFX/wiki/Known-Issues */
    System.setProperty("prism.lcdtext", "false");
    System.setProperty("prism.text", "t2k");


    /* *************************************************************************
     * INITIALIZE FXMLs
     * ************************************************************************/

    /* Create root BorderPane */
    BorderPane root = new BorderPane();

    /* Prepare VBox for menuBar and toolBar (top of BorderPane) */
    VBox menuBox = new VBox();
    root.setTop(menuBox);

    /* Initialize menuBar (top of VBox) */
    FXMLLoader menuLoader = new FXMLLoader(getClass().getClassLoader()
            .getResource("fxml/menuBar.fxml"));
    menuBox.getChildren().add(menuLoader.load());
    MenuBarController menuController = menuLoader.getController();

    /* Initialize toolBar (bottom of VBox) */
    FXMLLoader toolLoader = new FXMLLoader(getClass().getClassLoader()
            .getResource("fxml/toolBar.fxml"));
    menuBox.getChildren().add(toolLoader.load());
    ToolBarController toolController = toolLoader.getController();

    /* Initialize statusBar (bottom of BorderPane) */
    FXMLLoader statusLoader = new FXMLLoader(getClass().getClassLoader()
            .getResource("fxml/statusBar.fxml"));
    root.setBottom(statusLoader.load());
    StatusBarController statusBarController = statusLoader.getController();

    /* Prepare SplitPane in the center of the root BorderPane */
    SplitPane centeredSplitPane = new SplitPane();
    centeredSplitPane.setOrientation(Orientation.HORIZONTAL);
    centeredSplitPane.setDividerPositions(0.30);
    root.setCenter(centeredSplitPane);

    /* Prepare sideBar (left part of centeredSplitPane) */
    AnchorPane sideBar = new AnchorPane();
    SplitPane sidebarSplitPane = new SplitPane();
    sidebarSplitPane.setOrientation(Orientation.VERTICAL);
    sidebarSplitPane.setDividerPositions(0.5);
    sideBar.getChildren().add(sidebarSplitPane);
    AnchorPane.setTopAnchor(sidebarSplitPane, 0.0);
    AnchorPane.setRightAnchor(sidebarSplitPane, 0.0);
    AnchorPane.setLeftAnchor(sidebarSplitPane, 0.0);
    AnchorPane.setBottomAnchor(sidebarSplitPane, 0.0);

    centeredSplitPane.getItems().add(sideBar);
    SplitPane.setResizableWithParent(sideBar, Boolean.TRUE);

    /* Initialize fileTreeView. */
    FXMLLoader fileTreeViewLoader = new FXMLLoader(getClass().getClassLoader()
            .getResource("fxml/fileTreeView.fxml"));
    sidebarSplitPane.getItems().add(fileTreeViewLoader.load());
    FileTreeViewController fileTreeViewController
            = fileTreeViewLoader.getController();

    /* Initialize ruleTreeView. */
    FXMLLoader ruleTreeViewLoader = new FXMLLoader(getClass().getClassLoader()
            .getResource("fxml/ruleTreeView.fxml"));
    sidebarSplitPane.getItems().add(ruleTreeViewLoader.load());
    RuleTreeViewController ruleTreeViewController
            = ruleTreeViewLoader.getController();



    /* *************************************************************************
     * INITIALIZE DATA MODEL
     * ************************************************************************/

    DataModel model = new DataModel(stage);


    /* *************************************************************************
     * INITIALIZE CONTROLLERS
     * ************************************************************************/

    menuController.init(model,
        (chosenFile) -> RuleTreeViewState
          .loadState(chosenFile, ruleTreeViewController.getTreeView()),
        (chosenFile) -> RuleTreeViewState
          .saveState(chosenFile, ruleTreeViewController.getTreeView()),
        stage,
        getHostServices()
        );
    toolController.init(model);
    model.setConnectionButton(toolController.vondaConnectionButton);
    statusBarController.initModel(model);
    fileTreeViewController.init(model);
    ruleTreeViewController.init(model);


    /* *************************************************************************
     * INITIALIZE RUDIBUGGER EDITOR (IF WANTED)
     * ************************************************************************/

    /* Initalize editor (right part of centeredSplitPane) */
    if (model.getEditor() instanceof RudibuggerEditor) {
      FXMLLoader editorLoader = new FXMLLoader(getClass().getClassLoader()
              .getResource("fxml/editor.fxml"));
      centeredSplitPane.getItems().add(editorLoader.load());
      RudibuggerEditorController editorController
              = editorLoader.getController();

      editorController.initModel(model);
    }


    /* *************************************************************************
     * PREPARE GUI
     * ************************************************************************/

    /* Bind stage to field */
    model.mainStage = stage;

    /* Define GUI */
    Scene scene = new Scene(root);
    scene.getStylesheets().add("/styles/Styles.css");
    stage.setTitle("Rudibugger");
    stage.setScene(scene);
    Image icon = new Image("file:src/main/resources/"
            + "icons/baggerschaufel_titlebar_32x32.png");
    stage.getIcons().add(icon);
    root.setStyle(model.globalConf.getGlobalFontSizeAsStyle());

    /* Link splitpanes to model.layout */
    model.layout.addSplitPane(sidebarSplitPane, DIVIDER_SIDEBAR);
    model.layout.addSplitPane(centeredSplitPane, DIVIDER_SIDEBAR_EDITOR);

    /* Restore the layout and set listener */
    model.layout.restoreWindowPosition();
    model.layout.setStageCloseListener();
    model.layout.restoreDividerPositions();

    /* Show Rudibugger */
    stage.show();
    log.info("Rudibugger has been started");

    Parameters params = getParameters();

    if (params.getRaw().isEmpty()) {
      /* Open last openend project (if any) */
      if (model.globalConf.getLastOpenedProject() != null) {
        log.info("Opening previous project...");
        model.openProject(model.globalConf.getLastOpenedProject());
      }
    } else {
      log.info("Opening project from the command line ...");
      model.openProject(Path.of(params.getRaw().get(0)).toAbsolutePath());
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

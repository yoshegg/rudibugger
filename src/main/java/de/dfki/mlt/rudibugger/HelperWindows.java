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
import de.dfki.mlt.rudibugger.view.menuBar.AboutController;
import de.dfki.mlt.rudibugger.view.menuBar.SettingsController;
import de.dfki.mlt.rudibugger.editor.EmacsConnection;
import de.dfki.mlt.rudibugger.editor.Editor;
import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudibugger.project.VondaRuntimeConnection;
import de.dfki.mlt.rudibugger.searchAndFind.SearchController;
import de.dfki.mlt.rudibugger.view.ruleLoggingTableView.RuleLoggingTableViewController;
import static de.dfki.mlt.rudimant.common.Constants.RULE_FILE_EXTENSION;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class contains methods that are called to <br>
 *   - open dialogs that only have a simple purpose, and <br>
 *   - additional windows that have more functionality (search, settings, ...).
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public final class HelperWindows {

  static Logger log = LoggerFactory.getLogger("HelperWin");


  /* ***************************************************************************
   * COMMON METHODS
   * **************************************************************************/

  /**
   * Creates a window linked to the main stage and with an .fxml defined
   * AnchorPane as background.
   *
   * @param mainStage The main stage of rudibugger
   * @param page The .fxml defined AnchorPane
   * @return The stage of the new window
   */
  private static Stage createWindow(Stage mainStage, AnchorPane page,
          String title) {
    Stage stage = new Stage();
    stage.setTitle(title);
    stage.initModality(Modality.NONE);
    stage.initOwner(mainStage);
    stage.setScene(new Scene(page));
    stage.setOnCloseRequest(value -> stage.close());
    return stage;
  }


  /* ***************************************************************************
   * SETTINGS WINDOW
   * **************************************************************************/

  /** Represents the stage of a potential settings window. */
  private static Stage _settingsWindow;

  /** Shows the settings window (and creates it if needed). */
  public static void showSettingsWindow(Stage mainStage,
          GlobalConfiguration globalConf, EmacsConnection emacs) {
    if (_settingsWindow == null)
      _settingsWindow = createSettingsWindow(mainStage, globalConf, emacs);
    _settingsWindow.show();
  }

  /** Creates a new settings window. */
  private static Stage createSettingsWindow(Stage mainStage,
          GlobalConfiguration globalConf, EmacsConnection emacs) {

    /* Load .fxml file */
    AnchorPane page;
    FXMLLoader loader;
    try {
       loader = new FXMLLoader(HelperWindows.class
        .getResource("/fxml/settings.fxml"));
       page = (AnchorPane) loader.load();
    } catch (IOException e) {
      log.error(e.toString());
      return null;
    }

    /* Set controller */
    SettingsController controller = loader.getController();
    controller.init(globalConf);

    Stage settingsStage = createWindow(mainStage, page, "Settings");

    return settingsStage;
  }


  /* ***************************************************************************
   * RULE LOGGING WINDOW
   * **************************************************************************/

  private static Stage _ruleLoggingWindow;

  public static void showRuleLoggingWindow(Stage mainStage,
      Project project, Editor editor, GlobalConfiguration globalConf) {
    if (_ruleLoggingWindow == null)
      _ruleLoggingWindow = createRuleLoggingWindow(mainStage, project, editor,
        globalConf);
    _ruleLoggingWindow.show();
  }

  public static void closeRuleLoggingWindow() {
    if (_ruleLoggingWindow != null && _ruleLoggingWindow.isShowing())
      _ruleLoggingWindow.hide();
  }

  private static Stage createRuleLoggingWindow(Stage mainStage,
    Project project, Editor editor, GlobalConfiguration globalConf) {

    AnchorPane page = new AnchorPane();
    TableView table = new TableView();
    page.getChildren().add(table);
    AnchorPane.setTopAnchor(table, 0.0);
    AnchorPane.setRightAnchor(table, 0.0);
    AnchorPane.setLeftAnchor(table, 0.0);
    AnchorPane.setBottomAnchor(table, 0.0);

    RuleLoggingTableViewController controller
      = new RuleLoggingTableViewController();
    controller.init(project, editor, globalConf, table);

    Stage ruleLoggingState = createWindow(mainStage, page, "Rule logging");
    ruleLoggingState.setHeight(400);
    ruleLoggingState.setWidth(600);
    return ruleLoggingState;
  }


  /* ***************************************************************************
   * ABOUT WINDOW
   * **************************************************************************/

  /** Represents the stage of a potential about window. */
  private static Stage _aboutWindow;

  /** Shows the settings window (and creates it if needed). */
  public static void showAboutWindow(Stage mainStage) {
    if (_aboutWindow == null)
      _aboutWindow = createAboutWindow(mainStage);
    _aboutWindow.show();
  }

  private static Stage createAboutWindow(Stage mainStage) {

    /* Load .fxml file */
    AnchorPane page;
    FXMLLoader loader;
    try {
       loader = new FXMLLoader(HelperWindows.class
        .getResource("/fxml/about.fxml"));
       page = (AnchorPane) loader.load();
    } catch (IOException e) {
      log.error(e.toString());
      return null;
    }

    /* Set controller */
    AboutController controller = loader.getController();
    controller.init();

    Stage aboutStage = createWindow(mainStage, page, "About Rudibugger");
    aboutStage.setResizable(false);

    return aboutStage;
  }


  /*****************************************************************************
   * SEARCH WINDOW
   ****************************************************************************/

  public static void openSearchWindow(Stage mainStage, Editor editor,
    Path searchPath) {

    /* Load .fxml file */
    AnchorPane page;
    FXMLLoader loader;
    try {
       loader = new FXMLLoader(HelperWindows.class
        .getResource("/fxml/findInProjectWindow.fxml"));
       page = (AnchorPane) loader.load();
    } catch (IOException e) {
      log.error(e.toString());
      return;
    }

    /* Set controller */
    SearchController controller = loader.getController();
    controller.init(editor, searchPath);

    Stage findStage = createWindow(mainStage, page, "Find in project...");
    findStage.show();
  }


  /*****************************************************************************
   * DIALOG WINDOWS
   ****************************************************************************/

  /**
   * Asks the user what to do if there is already an open project.
   *
   * @param projectName The currently open project's name
   * @return true, if current project should be overwritten, else false
   */
  public static boolean openOverwriteProjectCheckDialog(String projectName) {
    log.debug("Asking what should happen because of the open project.");

    /* defining an Alert Window */
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Open Project...");
    alert.setHeaderText(projectName + " is already open.");
    alert.setContentText("Do you want to close the current project and \n"
                       + "open the new one instead?");

    /* defining its buttons */
    ButtonType close_and_open = new ButtonType("Close & Open");
    ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(close_and_open, cancel);

    /* open the window and bind its choice to variable */
    Optional<ButtonType> result = alert.showAndWait();

    /* define return value */
    if (result.get() == close_and_open)
      return OVERWRITE_PROJECT;
    else
      return !OVERWRITE_PROJECT;
  }

  /**
   * Asks the user what should happen to the modified but unsaved file if a
   * close request has been sent.
   *
   * @param fileName
   *        The name of the modified file
   * @return Integer representing the user's choice
   */
  public static int openCloseFileWithoutSavingCheckDialog(String fileName) {
    log.debug("Asking how an unsaved file should be closed.");

    /* Defining an Alert window. */
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle("Close unsaved file?");
    alert.setHeaderText(fileName + " has unsaved changes.");
    alert.setContentText("Do you want to save the file before closing it?");

    /* Defining the buttons. */
    ButtonType discard_changes
            = new ButtonType("Discard", ButtonData.CANCEL_CLOSE);
    ButtonType save_and_close = new ButtonType("Save", ButtonData.OK_DONE);
    ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(save_and_close, cancel, discard_changes);

     /* Open the window and bind its choice to variable. */
    Optional<ButtonType> result = alert.showAndWait();

    /* Return the correct Integer value. */
    if (result.get() == save_and_close)
      return CLOSE_BUT_SAVE_FIRST;
    else if (result.get() == discard_changes)
      return CLOSE_WITHOUT_SAVING;
    else
      return CANCEL_CLOSING;
  }


  /**
   * Opens a window to select a <code>.yml</code> project file.
   *
   * @param stage
   * @return The Path of the chosen configuration file
   */
  public static Path openYmlProjectFileDialog(Stage stage) {
    /* Defining the file chooser */
    log.debug("Preparing project chooser...");
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(new File(System.getProperty("user.home")));
    chooser.getExtensionFilters().addAll(new FileChooser
            .ExtensionFilter("YAML files (*.yml)", "*.yml", "*.YML"));
    chooser.setTitle("Open Project...");

    /* open the FileChooser */
    File chosenYmlFile = chooser.showOpenDialog(stage);

    /* abort selection if window has been closed */
    if (chosenYmlFile == null) {
      log.debug("Aborted selection of .yml file.");
      return null;
    }

    /* return converted .yml file as a Path object */
    return chosenYmlFile.toPath();
  }

  /**
   * Opens a window to select RuleModelState file.
   *
   * @param stage The stage of rudibugger
   * @param ruleLogSavePath The folder containing the ruleModelState files
   * @return The Path of the chosen configuration file
   */
  public static Path openRuleLoggingStateFileDialog(Stage stage,
    Path ruleLogSavePath) {
    /* Defining the file chooser */
    log.debug("Preparing ruleLoggingState file chooser...");
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(ruleLogSavePath.toFile());
    chooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("YAML files (*.yml)", "*.yml", "*.YML"));
    chooser.setTitle("Open ruleLoggingState file...");

    /* open the FileChooser */
    File chosenRuleLoggingStateFile = chooser.showOpenDialog(stage);
    log.debug("RuleModelState file chooser has been opened.");

    /* abort selection if window has been closed */
    if (chosenRuleLoggingStateFile == null) {
      log.debug("Aborted selection of ruleLoggingState file.");
      return null;
    }

    /* return chosen file as Path object */
    return chosenRuleLoggingStateFile.toPath();
  }

  /**
   * Opens a window to select where to store a new .rudi file and how to name
   * it.
   *
   * @param stage The stage of rudibugger
   * @param rudiFolder The Path of the .rudi folder
   * @return The path of a new file with .rudi extension
   */
  public static Path openSaveNewFileAsDialog(Stage stage, Path rudiFolder) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(rudiFolder.toFile());
    FileChooser.ExtensionFilter extFilter
            = new FileChooser.ExtensionFilter
        ("rudi file (*" + RULE_FILE_EXTENSION + ")", "*" + RULE_FILE_EXTENSION);
    fileChooser.getExtensionFilters().add(extFilter);
    Path newRudiFile;
    try {
      newRudiFile = (fileChooser.showSaveDialog(stage)).toPath();
    } catch (NullPointerException e) {
      return null;
    }
    if (newRudiFile == null) return null; // TODO: might be unnecessary
    if (! newRudiFile.getFileName().toString().endsWith(RULE_FILE_EXTENSION)) {
      newRudiFile = Paths.get(newRudiFile.toString() + RULE_FILE_EXTENSION);
    }
    return newRudiFile;
  }

  /**
   * Opens a window to select where to store a new ruleModelState file and how
   * to name it.
   *
   * @param stage The stage of rudibugger
   * @param rudiFolder The Path of the ruleModelStates folder
   * @return The path of a new file with .yml extension
   */
  public static Path openSaveRuleModelStateDialog(Stage stage,
          Path stateFolder) {
    if (! Files.exists(stateFolder)) stateFolder.toFile().mkdirs();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(stateFolder.toFile());
    FileChooser.ExtensionFilter extFilter
            = new FileChooser.ExtensionFilter
        ("YAML file (*" + "yml" + ")", "*" + "yml");
    fileChooser.getExtensionFilters().add(extFilter);
    Path file;
    try {
      file = (fileChooser.showSaveDialog(stage)).toPath();
    } catch (NullPointerException e) {
      return null;
    }

    if (!file.getFileName().toString().endsWith(".yml")) {
      file = Paths.get(file.toString() + ".yml");
    }

    return file;

  }
}

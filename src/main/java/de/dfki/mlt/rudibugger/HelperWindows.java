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
import de.dfki.mlt.rudibugger.Controller.AboutController;
import de.dfki.mlt.rudibugger.Controller.SettingsController;
import de.dfki.mlt.rudibugger.SearchAndFind.SearchController;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class contains static methods that are called to <br>
 *   - open dialogs that only have a simple purpose, and <br>
 *   - additional windows that have more functionality (search, settings, ...).
 *
 * Properties are used to disallow more than one window of a kind.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public final class HelperWindows {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("HelperWin");

  /** The <code>DataModel</code> */
  private final DataModel _model;

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public HelperWindows(DataModel model) {
    _model = model;
  }

  /*****************************************************************************
   * PROPERTIES / FIELDS
   ****************************************************************************/

  /** Indicates if the settings window is open. */
  private boolean settingsWindowOpened = false;

  /** Indicates if the about window is open. */
  private boolean aboutWindowOpened = false;

  /** Indicates if the search in project window is open. */
  private boolean searchInProjectWindowOpened = false;


  /*****************************************************************************
   * STATIC METHODS
   ****************************************************************************/

  /**
   * Asks the user what to do if there is already an open project.
   *
   * @param projectName The currently open project's name
   * @return true, if current project should be overwritten, else false
   */
  public static boolean overwriteProjectCheck(String projectName) {
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
  public static int closeFileWithoutSavingCheck(String fileName) {
    log.debug("Asking how an unsaved file should be closed.");

    /* Defining an Alert window. */
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle("Close unsaved file?");
    alert.setHeaderText(fileName + " has unsaved changes.");
    alert.setContentText("Do you want to save the file before closing it?");

    /* Defining the buttons. */
    ButtonType discard_changes = new ButtonType("Discard", ButtonData.CANCEL_CLOSE);
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
  public static Path openYmlProjectFile(Stage stage) {
    /* Defining the file chooser */
    log.debug("Preparing project chooser...");
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(new File(System.getProperty("user.home")));
    chooser.getExtensionFilters().addAll(new FileChooser
            .ExtensionFilter("YAML files (*.yml)", "*.yml", "*.YML"));
    chooser.setTitle("Open Project...");

    /* open the FileChooser */
    File chosenYmlFile = chooser.showOpenDialog(stage);
    log.debug("Project chooser has been opened.");

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
  public static Path openRuleLoggingStateFile(Stage stage,
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


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Opens the about window. */
  public void openAboutWindow() {
    if (aboutWindowOpened) return;
    try {
      /* Load the fxml file and create a new stage for the about window */
      FXMLLoader loader = new FXMLLoader(HelperWindows.class
        .getResource("/fxml/about.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage aboutStage = new Stage();
      aboutStage.setResizable(false);
      aboutStage.setTitle("About Rudibugger");
      aboutStage.initModality(Modality.NONE);
      aboutStage.initOwner(_model.stageX);
      Scene scene = new Scene(page);
      aboutStage.setScene(scene);

      /* Set the controller */
      AboutController controller = loader.getController();
      controller.initModel(_model);
      controller.setDialogStage(aboutStage);

      /* show the dialog */
      aboutStage.show();
      aboutWindowOpened = true;

      /* handle close request */
      aboutStage.setOnCloseRequest(value -> aboutWindowOpened = false);

    } catch (IOException e) {
      log.error(e.toString());
    }
  }

  /** Opens the settings menu. */
  public void openSettingsDialog() {
    if (settingsWindowOpened) return;
    try {
      /* Load the fxml file and create a new stage for the settings dialog */
      FXMLLoader loader = new FXMLLoader(HelperWindows.class
        .getResource("/fxml/settings.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage settingsStage = new Stage();
      settingsStage.setTitle("Settings");
      settingsStage.initModality(Modality.NONE);
      settingsStage.initOwner(_model.stageX);
      Scene scene = new Scene(page);
      settingsStage.setScene(scene);

      /* Set the controller */
      SettingsController controller = loader.getController();
      controller.init(_model);

      /* show the dialog */
      settingsStage.show();
      settingsWindowOpened = true;

      /* handle close request */
      settingsStage.setOnCloseRequest(value -> settingsWindowOpened = false);

    } catch (IOException e) {
      log.error(e.toString());
    }
  }

  /** Opens the search window. */
  public void openSearchWindow() {
    if (searchInProjectWindowOpened) return;
    try {
      /* Load the fxml file and create a new stage for the about window */
      FXMLLoader loader = new FXMLLoader(HelperWindows.class
        .getResource("/fxml/findInProjectWindow.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage findStage = new Stage();
      findStage.setTitle("Find in project...");
      findStage.initModality(Modality.NONE);
      findStage.initOwner(_model.stageX);
      Scene scene = new Scene(page);
      findStage.setScene(scene);

      /* Set the controller */
      SearchController controller = loader.getController();
      controller.initModel(_model);

      /* show the dialog */
      findStage.show();
      searchInProjectWindowOpened = true;

      /* handle close request */
      findStage.setOnCloseRequest(value -> searchInProjectWindowOpened = false);

    } catch (IOException e) {
      log.error(e.toString());
    }
  }

}

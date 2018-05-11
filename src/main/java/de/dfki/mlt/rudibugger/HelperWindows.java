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
import static de.dfki.mlt.rudibugger.DataModel.log;
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
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public final class HelperWindows {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("HelperWin");

  /** Private constructor to obstruct instantiating of this utility class. */
  private HelperWindows() {}


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /**
   * Asks the user what to do if there is already an open project.
   *
   * @param projectName The currently open project's name
   * @return Integer stating to replace current project, open new window or do
   * nothing
   */
  public static int overwriteProjectCheck(String projectName) {
    log.debug("Asking what should happen because of the open project.");

    /* defining an Alert Window */
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Open Project...");
    alert.setHeaderText(projectName + " is already open.");
    alert.setContentText("Do you want to close the current project and \n"
                       + "open the new one in this window or do you \n"
                       + "want to open a new window instead?");

    /* defining its buttons */
    ButtonType currentWindow = new ButtonType("Close");
    ButtonType newWindow = new ButtonType("New");
    ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(currentWindow, newWindow, cancel);

    /* TODO: remove this if the according functions are implemented */
    alert.getDialogPane().lookupButton(currentWindow).setDisable(false);
    alert.getDialogPane().lookupButton(newWindow).setDisable(true);

    /* open the window and bind its choice to variable */
    Optional<ButtonType> result = alert.showAndWait();

    /* define return value */
    if (result.get() == currentWindow)
      return OVERWRITE_CHECK_CURRENT_WINDOW;
    else if (result.get() == newWindow)
      return OVERWRITE_CHECK_NEW_WINDOW;
    else
      return OVERWRITE_CHECK_CANCEL;
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

  /**
   * Opens the about window.
   *
   * @param model The current <code>DataModel</code>
   */
  public static void openAboutWindow(DataModel model) {
    try {
      /* Load the fxml file and create a new stage for the about window */
      FXMLLoader loader = new FXMLLoader(HelperWindows.class
        .getResource("/fxml/about.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage aboutStage = new Stage();
      aboutStage.setResizable(false);
      aboutStage.setTitle("About Rudibugger");
      aboutStage.initModality(Modality.NONE);
      aboutStage.initOwner(model.stageX);
      Scene scene = new Scene(page);
      aboutStage.setScene(scene);

      /* Set the controller */
      AboutController controller = loader.getController();
      controller.initModel(model);
      controller.setDialogStage(aboutStage);

      /* show the dialog */
      aboutStage.show();

    } catch (IOException e) {
      log.error(e.toString());
    }
  }

  /**
   * Opens the settings menu.
   *
   * @param model The current <code>DataModel</code>
   */
  public static void openSettingsDialog(DataModel model) {
    try {
      /* Load the fxml file and create a new stage for the settings dialog */
      FXMLLoader loader = new FXMLLoader(HelperWindows.class
        .getResource("/fxml/settings.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage settingsStage = new Stage();
      settingsStage.setTitle("Settings");
      settingsStage.initModality(Modality.NONE);
      settingsStage.initOwner(model.stageX);
      Scene scene = new Scene(page);
      settingsStage.setScene(scene);

      /* Set the controller */
      SettingsController controller = loader.getController();
      controller.initModel(model);
      controller.setDialogStage(settingsStage);

      /* show the dialog */
      settingsStage.show();

    } catch (IOException e) {
      log.error(e.toString());
    }
  }

  /**
   * Opens the search window.
   *
   * @param model The current <code>DataModel</code>
   */
  public static void openSearchWindow(DataModel model) {
    try {
      /* Load the fxml file and create a new stage for the about window */
      FXMLLoader loader = new FXMLLoader(HelperWindows.class
        .getResource("/fxml/findInProjectWindow.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage findStage = new Stage();
      findStage.setTitle("Find in project...");
      findStage.initModality(Modality.NONE);
      findStage.initOwner(model.stageX);
      Scene scene = new Scene(page);
      findStage.setScene(scene);

      /* Set the controller */
      SearchController controller = loader.getController();
      controller.initModel(model);

      /* show the dialog */
      findStage.show();

    } catch (IOException e) {
      log.error(e.toString());
    }
  }

}

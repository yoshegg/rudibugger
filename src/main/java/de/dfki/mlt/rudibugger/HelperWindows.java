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
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * TODO: COMPLETE DOCUMENTATION.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public final class HelperWindows {

  /** the logger */
  static Logger log = LoggerFactory.getLogger("HelperWin");

  /** private constructor: obstruct instantiating this utility class */
  private HelperWindows() {}

  /**
   * This function asks the user what to do if there is already an open project.
   *
   * @param model
   * @return Integer stating to replace current project, open new window or do
   * nothing
   */
  public static int overwriteProjectCheck(DataModel model) {
    log.debug("Asking what should happen because of the open project.");

    /* defining an Alert Window */
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Open Project...");
    alert.setHeaderText(model.project.getProjectName() + " is already open.");
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
    if (result.get() == currentWindow) {
      return OVERWRITE_CHECK_CURRENT_WINDOW;
    } else if (result.get() == newWindow) {
      return OVERWRITE_CHECK_NEW_WINDOW;
    } else {
      return OVERWRITE_CHECK_CANCEL;
    }
  }

  /**
   * This function helps the user to specify a .yml project file by opening
   * a FileChooser window.
   *
   * @param stage
   * @return
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

  public static Path openRuleLoggingStateFile(Stage stage,
    Path ruleLogSavePath) {
    /* Defining the file chooser */
    log.debug("Perparing ruleLoggingState file chooser...");
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(ruleLogSavePath.toFile());
    chooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("YAML files (*.yml)", "*.yml", "*.YML"));
    chooser.setTitle("Open ruleLoggingState file...");

    /* open the FileChooser */
    File chosenRuleLoggingStateFile = chooser.showOpenDialog(stage);
    log.debug("ruleLoggingState file chooser has been opened.");

    /* abort selection if window has been closed */
    if (chosenRuleLoggingStateFile == null) {
      log.debug("Aborted selection of ruleLoggingState file.");
      return null;
    }

    /* return chosen file as Path object */
    return chosenRuleLoggingStateFile.toPath();
  }
}

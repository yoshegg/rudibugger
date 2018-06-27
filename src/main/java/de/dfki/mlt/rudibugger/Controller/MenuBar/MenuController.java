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

package de.dfki.mlt.rudibugger.Controller.MenuBar;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.Controller.Controller;

import de.dfki.mlt.rudibugger.HelperWindows;
import de.dfki.mlt.rudibugger.MainApp;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.TabManagement.RudiTab;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller's purpose is to manage the MenuBar and the ToolBar of
 * rudibugger.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MenuController extends Controller {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("MenuController");


  /*****************************************************************************
   * EXTENSIONS OF THE MENU CONTROLLER
   ****************************************************************************/

  /**
   * Contains functionality to manage the look and file of the compile button.
   */
  private CompileButtonManager compileButtonManager;

  /**
   * Contains functionality to manage the look and file of the connection
   * button.
   */
  private ConnectionButtonManager connectionButtonManager;


  /*****************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   ****************************************************************************/

  /**
   * Initializes this controller.
   *
   * @param model The current <code>DataModel</code>
   */
  public void init(DataModel model) {
    linkModel(model);
    compileButtonManager = CompileButtonManager.init(_model, compileButton,
            toolBar);
    connectionButtonManager = ConnectionButtonManager.init(_model,
            vondaConnectionButton, toolBar);
    initModel();
  }


  /**
   * Connects this controller to the DataModel.
   *
   * @param model
   */
  public void initModel() {

    /* this listener checks for a run file */
    _model.project.runFileProperty().addListener((o, oldVal, newVal) -> {
      if (newVal != null) {
        log.debug("As a run file has been found, "
                + "the button was enabled.");
        runButton.setDisable(false);
      } else {
        runButton.setDisable(true);
      }
    });

    /* this listener checks if a project has been opened */
    _model.projectLoadedProperty().addListener((o, ov, nv) -> {
      if (nv) {
        log.debug("Project open: enable GUI-elements.");
        closeProjectItem.setDisable(false);
        newRudiFileItem.setDisable(false);
        loadLoggingStateMenu.setDisable(false);
        saveLoggingStateItem.setDisable(false);
        findInProjectItem.setDisable(false);
        connectionButtonManager.manageLookOfVondaConnectionButton();
        compileButtonManager.defineCompileButton();
      } else {
        log.debug("Project closed: disable GUI-elements.");
        closeProjectItem.setDisable(true);
        newRudiFileItem.setDisable(true);
        loadLoggingStateMenu.setDisable(true);
        saveLoggingStateItem.setDisable(true);
        findInProjectItem.setDisable(true);
        connectionButtonManager.manageLookOfVondaConnectionButton();
        compileButtonManager.defineCompileButton();
      }
    });

    /* Keep track of default compile command. */
    _model.project.defaultCompileCommandProperty().addListener((cl, ov, vn) -> {
      compileButtonManager.defineCompileButton();
    });

    _model.vonda.connectedProperty().addListener(l ->
      connectionButtonManager.manageLookOfVondaConnectionButton()
    );

    /* this listener enables saving depending on the selected tab */
    _model.tabStore.currentlySelectedTabProperty().addListener((o, oldVal, newVal) -> {

      /* no tab is opened */
      if (newVal == null) {
        saveItem.setDisable(true);
        saveAsItem.setDisable(true);
        saveAllItem.setDisable(true);

      /* one known tab is selected and can be saved */
      } else if (((RudiTab) newVal).isKnown()) {

        if (newVal.hasBeenModifiedProperty().getValue()) {
          saveItem.setDisable(false);
        } else {
          /* wait until the tab content has been modified */
          newVal.hasBeenModifiedProperty().addListener((o2, oldVal2, newVal2) -> {
            if (newVal2) {
              saveItem.setDisable(false);
            } else {
              saveItem.setDisable(true);
            }
          });
        }

        saveAsItem.setDisable(false);
        saveAllItem.setDisable(false);


      /* a newly created file can only be saved as */
      } else if (! ((RudiTab) newVal).isKnown()) {
        saveItem.setDisable(true);
        saveAsItem.setDisable(false);
        saveAllItem.setDisable(false);
      }
    });

    /* initalize the recent projets submenu... */
    if (! _model.globalConf.recentProjects.isEmpty()) {
      buildRecentProjectsMenu();
    }

    /* ... then keep track of changes */
    _model.globalConf.recentProjects.addListener(
            (ListChangeListener.Change<? extends String> c) -> {
      buildRecentProjectsMenu();
    });
  }

  private void buildRecentProjectsMenu() {
    openRecentProjectMenu.getItems().clear();
    _model.globalConf.recentProjects.forEach((x) -> {
      MenuItem mi = new MenuItem(x);
      mi.setOnAction((event) -> {
        checkForOpenProject(Paths.get(x));
      });
      openRecentProjectMenu.getItems().add(mi);
    });
  }

  /**
   * Builds the menu offering to load the 10 most recent RuleModelState
   * configurations.
   */
  @FXML
  private void buildLoadRuleSelectionStateMenu() {
    if (! _model.projectLoadedProperty().get()) return;
    if (!_model.ruleModelState.getRecentStates().isEmpty()) {
      loadLoggingStateMenu.getItems().clear();
      _model.ruleModelState.getRecentStates().forEach((x) -> {
        String filenameWithFolder = _model.project.getRuleModelStatesFolder()
                .relativize(x).toString();
        MenuItem mi = new MenuItem(filenameWithFolder);
        mi.setOnAction((event) -> {
          _model.ruleModelState.loadState(x);
        });
        loadLoggingStateMenu.getItems().add(mi);
      });
    } else {
      loadLoggingStateMenu.getItems().clear();
      loadLoggingStateMenu.getItems().add(noRecentConfigurationFound);
    }
    loadLoggingStateMenu.getItems().add(new SeparatorMenuItem());
    loadLoggingStateMenu.getItems().add(openRuleLoggingStateItem);
  }

  /**
   * This function is used to check for open projects
   *
   * @param ymlFile null, if the project has not been defined yet, else the Path
   * to the project's .yml file
   */
  private void checkForOpenProject(Path ymlFile) {

    /* a project is already open */
    if (_model.projectLoadedProperty().getValue() == PROJECT_OPEN) {
      if (OVERWRITE_PROJECT == HelperWindows.overwriteProjectCheck(
        _model.project.getProjectName())) {

        if (ymlFile == null)
          ymlFile = HelperWindows.openYmlProjectFile(_model.stageX);
        if (ymlFile == null)
          return;

        _model.close(true);
        _model.init(ymlFile);
      }
    }

    /* no project is open */
    else {
      if (ymlFile == null)
        ymlFile = HelperWindows.openYmlProjectFile(_model.stageX);
      if (ymlFile == null) {
        return;
      }
      _model.init(ymlFile);

    }
  }


  /*****************************************************************************
   * TOOLBAR BUTTONS
   ****************************************************************************/

  /* Represents the compile button. */
  @FXML
  private Button compileButton;

  /* Represents the run button. */
  @FXML
  private Button runButton;

  /**
   * Represents the con-/disconnect button also monitoring the connection state.
   */
  @FXML
  private Button vondaConnectionButton;


  /*****************************************************************************
   * Menu items actions (from menu bar)
   ****************************************************************************/

  @FXML
  private void findInProject(ActionEvent event) {
    _model.helperWindows.openSearchWindow();
  }

  @FXML
  private MenuItem findInProjectItem;

  /********* File *********/


  /** MenuItem "New Project..." */
  @FXML
  private MenuItem newProjectItem;

  /** Action "New Project..." */
  @FXML
  private void newProjectAction(ActionEvent event) {
    _model.createNewProject();
  }


  /** MenuItem "New rudi File..." */
  @FXML
  private MenuItem newRudiFileItem;

  /** Action "New rudi File..." */
  @FXML
  private void newRudiFileAction(ActionEvent event)
          throws FileNotFoundException {
    _model.rudiLoad.openFile(null);
  }


  /** MenuItem "Open Project..." */
  @FXML
  private MenuItem openProjectItem;

  /** Action "Open Project..." */
  @FXML
  private void openProjectAction(ActionEvent event)
          throws FileNotFoundException, IOException, IllegalAccessException {
    checkForOpenProject(null);
  }


  /** Menu "Open Recent Project" */
  @FXML
  private Menu openRecentProjectMenu;


  /** MenuItem "Close Project" */
  @FXML
  private MenuItem closeProjectItem;

  /** Action "Close Project" */
  @FXML
  private void closeProjectAction(ActionEvent event)
          throws FileNotFoundException {
    _model.close(false);
  }


  /** MenuItem "No recent configuration found. */
  @FXML
  private MenuItem noRecentConfigurationFound;

  /** MenuItem "Open configuration file... */
  @FXML
  private MenuItem openRuleLoggingStateItem;

  /** Menu "Load logging state" */
  @FXML
  private Menu loadLoggingStateMenu;

  /** Action "Open configuration file..." */
  @FXML
  private void openRuleLoggingStateConfigurationFile(ActionEvent event) {
    _model.ruleModelState.loadStateSelectFile();
  }

  /** MenuItem "Save logging state..." */
  @FXML
  private MenuItem saveLoggingStateItem;

  /** Action "Save logging state" */
  @FXML
  private void saveLoggingStateAction(ActionEvent event) {
    _model.ruleModelState.requestSave();
  }


  /** MenuItem "Save" */
  @FXML
  private MenuItem saveItem;

  /** Action "Save" */
  @FXML
  private void saveAction(ActionEvent event) {
    _model.rudiSave.quickSaveFile(
            _model.tabStore.currentlySelectedTabProperty().get());
  }


  /** MenuItem "Save as..." */
  @FXML
  private MenuItem saveAsItem;

  /** Action "Save as..." */
  @FXML
  private void saveAsAction(ActionEvent event) {
    _model.rudiSave.saveFileAs(
            _model.tabStore.currentlySelectedTabProperty().get());
  }


  /** MenuItem "Save all" */
  @FXML
  private MenuItem saveAllItem;

  /** Action "Save all" */
  @FXML
  private void saveAllAction(ActionEvent event) {
    _model.rudiSave.quickSaveAllFiles();
  }


  /** MenuItem "Exit" */
  @FXML
  private MenuItem exitItem;

  /** Action "Exit" */
  @FXML
  private void exitAction(ActionEvent event) {
    _model.layout.saveLayoutToFile();
    MainApp.exitRudibugger();
  }


  /********* Tools *********/
  @FXML
  private void openSettingsDialog(ActionEvent event) {
    _model.helperWindows.openSettingsDialog();
  }

  /********* Help *********/
  @FXML
  private void openAboutWindow(ActionEvent event) {
    _model.helperWindows.openAboutWindow();
  }


  /*****************************************************************************
   * Button actions & toolBar
  *****************************************************************************/

  /** Contains buttons */
  @FXML
  private ToolBar toolBar;

  /* Clicking the run button */
  @FXML
  private void startRun(ActionEvent event) {
    log.warn("\"Run\" is not implemented yet.");
  }

  /** Establishes a connection to the VOnDA server or disconnects from it. */
  @FXML
  private void changeVondaConnectionState(ActionEvent event) {
    int conStatus = _model.vonda.connectedProperty().get();
    if (conStatus == DISCONNECTED_FROM_VONDA)
      _model.vonda.connect();
    else
      _model.vonda.closeConnection();
  }

}

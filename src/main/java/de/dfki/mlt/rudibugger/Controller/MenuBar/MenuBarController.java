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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.HelperWindows;
import de.dfki.mlt.rudibugger.MainApp;
import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudibugger.TabManagement.RudiTab;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * This controller's purpose is to manage the MenuBar and the ToolBar of
 * rudibugger.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MenuBarController {

  static Logger log = LoggerFactory.getLogger("MenuBarController");

  private DataModel _model;

  /** Represents a potentially loaded project. */
//  private Project _project;


  /*****************************************************************************
   * EXTENSIONS OF THE MENU CONTROLLER
   ****************************************************************************/


  private Consumer<Path> loadRuleState, saveRuleState;

  /*****************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   ****************************************************************************/

  /** Initializes this controller. */
  public void init(DataModel model, Consumer<Path> lrs, Consumer<Path> srs) {
    loadRuleState = lrs;
    saveRuleState = srs;
    _model = model;


    listenForProject();
  }

  private void listenForProject() {
    _model.loadedProjectProperty().addListener((o, ov, project) -> {
      boolean projectLoaded = project != null;
      toggleProjectSpecificMenuItems(projectLoaded);

//      if (project != null) {
//        log.debug("Project open: enable GUI-elements.");
//        compileButtonManager.defineCompileButton(project, project.compiler);
//      } else {
//        log.debug("Project closed: disable GUI-elements.");
//        compileButtonManager.defineCompileButton(project, project.compiler); //TODO: project is not set anymore
//      }
    });
  }

  private void toggleProjectSpecificMenuItems(boolean projectLoaded) {
    boolean val = ! projectLoaded;
    newRudiFileItem.setDisable(val);
    closeProjectItem.setDisable(val);
    loadLoggingStateMenu.setDisable(val);
    saveLoggingStateItem.setDisable(val);
    findInProjectItem.setDisable(val);
  }

//  /** TODO
//   * Initializes the controller.
//   */
//  public void tabManagement() {
//
//    /* this listener enables saving depending on the selected tab */
//    _model.getLoadedProject().getTabStore().currentlySelectedTabProperty().addListener((o, oldVal, newVal) -> {
//
//      /* no tab is opened */
//      if (newVal == null) {
//        saveItem.setDisable(true);
//        saveAsItem.setDisable(true);
//        saveAllItem.setDisable(true);
//
//      /* one known tab is selected and can be saved */
//      } else if (((RudiTab) newVal).isKnown()) {
//
//        if (newVal.hasBeenModifiedProperty().getValue()) {
//          saveItem.setDisable(false);
//        } else {
//          /* wait until the tab content has been modified */
//          newVal.hasBeenModifiedProperty().addListener((o2, oldVal2, newVal2) -> {
//            if (newVal2) {
//              saveItem.setDisable(false);
//            } else {
//              saveItem.setDisable(true);
//            }
//          });
//        }
//
//        saveAsItem.setDisable(false);
//        saveAllItem.setDisable(false);
//
//
//      /* a newly created file can only be saved as */
//      } else if (! ((RudiTab) newVal).isKnown()) {
//        saveItem.setDisable(true);
//        saveAsItem.setDisable(false);
//        saveAllItem.setDisable(false);
//      }
//    });
//
//  }

  @FXML
  private void buildRecentProjectsMenu() {
    openRecentProjectMenu.getItems().clear();
    _model.globalConf.recentProjects.forEach((x) -> {
      MenuItem mi = new MenuItem(x);
      mi.setOnAction((event) -> {
        checkForOpenProject();
      });
      openRecentProjectMenu.getItems().add(mi);
    });
  }

  /**
   * Builds the menu offering to load the 10 most recent RuleTreeViewState
 configurations.
   */
  @FXML
  private void buildLoadRuleSelectionStateMenu() {
    Project project = _model.getLoadedProject();
    if (project == null) return;

    if (!project.getRecentStates().isEmpty()) {
      loadLoggingStateMenu.getItems().clear();
      project.getRecentStates().forEach((x) -> {
        String filenameWithFolder = project.getRuleModelStatesFolder()
                .relativize(x).toString();
        MenuItem mi = new MenuItem(filenameWithFolder);
        mi.setOnAction((event) -> { loadRuleState.accept(x); });
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
   * TODO: Should probably be somewhere else
   *
   * @param ymlFile null, if the project has not been defined yet, else the Path
   * to the project's .yml file
   */
  private boolean checkForOpenProject() {

    /* a project is already open */
    if (_model.isProjectLoaded()) {
      return OVERWRITE_PROJECT == HelperWindows.openOverwriteProjectCheckDialog(
              _model.getLoadedProject().getProjectName());
    }
    return true;
  }

  /*****************************************************************************
   * Menu items actions (from menu bar)
   ****************************************************************************/

  @FXML
  private void findInProject(ActionEvent event) {
    HelperWindows.openSearchWindow(_model.mainStage, _model.getLoadedProject());
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
    _model.getLoadedProject().openFile(null);
  }


  /** MenuItem "Open Project..." */
  @FXML
  private MenuItem openProjectItem;

  /** Action "Open Project..." */
  @FXML
  private void openProjectAction(ActionEvent event)
          throws FileNotFoundException, IOException, IllegalAccessException {
    if (checkForOpenProject()) {
      Path projectYml = HelperWindows.openYmlProjectFileDialog(_model.mainStage);
      _model.openProject(projectYml);
    }
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
    _model.closeProject();
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
    Path saveFolder = _model.getLoadedProject().getRuleModelStatesFolder();
    Path chosenFile = HelperWindows.openRuleLoggingStateFileDialog(
            _model.mainStage, saveFolder);
    if (chosenFile == null) return;
    loadRuleState.accept(chosenFile);
  }

  /** MenuItem "Save logging state..." */
  @FXML
  private MenuItem saveLoggingStateItem;

  /** Action "Save logging state" */
  @FXML
  private void saveLoggingStateAction(ActionEvent event) {
    Path saveFolder = _model.getLoadedProject().getRuleModelStatesFolder();
    Path newStateFile = HelperWindows.openSaveRuleModelStateDialog(
            _model.mainStage, saveFolder);
    saveRuleState.accept(newStateFile);
  }


  /** MenuItem "Save" */
  @FXML
  private MenuItem saveItem;

  /** Action "Save" */
  @FXML
  private void saveAction(ActionEvent event) {
    _model.getLoadedProject().quickSaveFile(
            _model.getLoadedProject().getTabStore().currentlySelectedTabProperty().get());
  }


  /** MenuItem "Save as..." */
  @FXML
  private MenuItem saveAsItem;

  /** Action "Save as..." */
  @FXML
  private void saveAsAction(ActionEvent event) {
    RudiTab currentTab = _model.getLoadedProject().getTabStore().currentlySelectedTabProperty().get();


//    _model.getLoadedProject().saveFileAs( // TODO
//            _model.getLoadedProject().getTabStore().currentlySelectedTabProperty().get());
  }

  /**
   * Save tab's content into a new file.
   * TODO: Should be somewhere else
   * @return True, if the file has been successfully saved, else false
   */
  public boolean saveFileAs(RudiTab tab) {
    Project project = _model.getLoadedProject();
    String content = tab.getRudiCode();

    Path newRudiFile = HelperWindows.openSaveNewFileAsDialog(
            _model.mainStage, project.getRudiFolder());

    if (project.saveFile(newRudiFile, content)) {
      tab.setText(newRudiFile.getFileName().toString());
      project.getTabStore().openTabsProperty().get().remove(tab.getFile());
      tab.setFile(newRudiFile);
      project.getTabStore().openTabsProperty().get().put(newRudiFile, tab);
      tab.waitForModifications();

      log.debug("File " + newRudiFile.getFileName() + " has been saved.");
      return true;
    }
    return false;
  }


  /** MenuItem "Save all" */
  @FXML
  private MenuItem saveAllItem;

  /** Action "Save all" */
  @FXML
  private void saveAllAction(ActionEvent event) {
    _model.getLoadedProject().quickSaveAllFiles();
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
    HelperWindows.showSettingsWindow(_model.mainStage, _model.globalConf, _model.emacs);
  }

  /********* Help *********/
  @FXML
  private void openAboutWindow(ActionEvent event) {
    HelperWindows.showAboutWindow(_model.mainStage);
  }

}

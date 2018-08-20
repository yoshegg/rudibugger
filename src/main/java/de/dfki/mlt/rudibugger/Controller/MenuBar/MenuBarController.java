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
import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * This controller's purpose is to manage the MenuBar and the ToolBar of
 * rudibugger.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MenuBarController {

  static Logger log = LoggerFactory.getLogger("MenuBarController");

  private DataModel _model;

  private Consumer<Path> loadRuleState, saveRuleState;

  private Stage _mainStage;

  /** Needed to open web pages. */
  private HostServices _hostServices;


  /*****************************************************************************
   * GUI ITEMS
   ****************************************************************************/

  @FXML
  private MenuItem newRudiFileItem;

  @FXML
  private Menu openRecentProjectMenu;

  @FXML
  private MenuItem closeProjectItem;


  @FXML
  private Menu loadRuleLoggingStateMenu;

  @FXML
  private MenuItem openRuleLoggingStateItem;

  @FXML
  private MenuItem saveLoggingStateItem;


  @FXML
  private MenuItem saveItem;

  @FXML
  private MenuItem saveAsItem;

  @FXML
  private MenuItem saveAllItem;


  @FXML
  private MenuItem findInProjectItem;


  /* ***************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   * **************************************************************************/

  /** Initializes this controller. */
  public void init(DataModel model, Consumer<Path> lrs, Consumer<Path> srs,
    Stage mainStage, HostServices hostServices) {
    loadRuleState = lrs;
    saveRuleState = srs;
    _mainStage = mainStage;
    _model = model;
    _hostServices = hostServices;

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


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  private void toggleProjectSpecificMenuItems(boolean projectLoaded) {
    boolean val = ! projectLoaded;
    newRudiFileItem.setDisable(val);
    closeProjectItem.setDisable(val);
    loadRuleLoggingStateMenu.setDisable(val);
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
        _model.openProject(Paths.get(x));
      });
      openRecentProjectMenu.getItems().add(mi);
    });
  }

  /**
   * Builds the menu offering to load the 10 most recent RuleTreeViewState
   * configurations.
   */
  @FXML
  private void buildLoadRuleSelectionStateMenu() {
    Project project = _model.getLoadedProject();
    if (project == null) return;

    if (!project.getRecentStates().isEmpty()) {
      loadRuleLoggingStateMenu.getItems().clear();
      project.getRecentStates().forEach((x) -> {
        String filenameWithFolder = project.getRuleModelStatesFolder()
                .relativize(x).toString();
        MenuItem mi = new MenuItem(filenameWithFolder);
        mi.setOnAction((event) -> { loadRuleState.accept(x); });
        loadRuleLoggingStateMenu.getItems().add(mi);
      });
    } else {
      loadRuleLoggingStateMenu.getItems().clear();
      loadRuleLoggingStateMenu.getItems()
        .add(new MenuItem("No recent configuration found."));
    }
    loadRuleLoggingStateMenu.getItems().add(new SeparatorMenuItem());
    loadRuleLoggingStateMenu.getItems().add(openRuleLoggingStateItem);
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


  /* ***************************************************************************
   * MENU ACTIONS
   * **************************************************************************/

  /* ***************************************************************************
   * FILE
   * **************************************************************************/

  @FXML
  private void newProjectAction(ActionEvent event) {
    _model.createNewProject();
  }

  @FXML
  private void newRudiFileAction(ActionEvent event)
          throws FileNotFoundException {
    _model.getLoadedProject().openFile(null);
  }


  @FXML
  private void openProjectAction(ActionEvent event)
          throws FileNotFoundException, IOException, IllegalAccessException {
    Path projectYml = HelperWindows.openYmlProjectFileDialog(_mainStage);
    _model.openProject(projectYml);
  }

  @FXML
  private void closeProjectAction(ActionEvent event)
          throws FileNotFoundException {
    _model.closeProject();
  }


  @FXML
  private void openRuleLoggingStateConfigurationFile(ActionEvent event) {
    Path saveFolder = _model.getLoadedProject().getRuleModelStatesFolder();
    Path chosenFile = HelperWindows.openRuleLoggingStateFileDialog(
            _model.mainStage, saveFolder);
    if (chosenFile == null) return;
    loadRuleState.accept(chosenFile);
  }

  @FXML
  private void saveLoggingStateAction(ActionEvent event) {
    Path saveFolder = _model.getLoadedProject().getRuleModelStatesFolder();
    Path newStateFile = HelperWindows.openSaveRuleModelStateDialog(
            _model.mainStage, saveFolder);
    saveRuleState.accept(newStateFile);
  }


  @FXML
  private void saveAction(ActionEvent event) {
    _model.getLoadedProject().quickSaveFile(
            _model.getLoadedProject().getTabStore()
              .currentlySelectedTabProperty().get());
  }

  @FXML
  private void saveAsAction(ActionEvent event) {
    RudiTab currentTab = _model.getLoadedProject().getTabStore()
      .currentlySelectedTabProperty().get();


//    _model.getLoadedProject().saveFileAs( // TODO
//            _model.getLoadedProject().getTabStore().currentlySelectedTabProperty().get());
  }

  @FXML
  private void saveAllAction(ActionEvent event) {
    _model.getLoadedProject().quickSaveAllFiles();
  }


  @FXML
  private void exitAction(ActionEvent event) {
    _model.layout.saveLayoutToFile();
    MainApp.exitRudibugger();
  }


  /* ***************************************************************************
   * EDIT
   * **************************************************************************/

  @FXML
  private void findInProject(ActionEvent event) {
    HelperWindows.openSearchWindow(_mainStage, _model.getLoadedProject());
  }


  /* ***************************************************************************
   * TOOLS
   * **************************************************************************/

  @FXML
  private void openSettingsDialog(ActionEvent event) {
    HelperWindows.showSettingsWindow(_mainStage, _model.globalConf,
      _model.emacs);
  }


  /* ***************************************************************************
   * HELP
   * **************************************************************************/

  @FXML
  private void openHelp(ActionEvent event) {
    try {
      _hostServices.showDocument(new URL(HELP_URL).toURI().toString());
    } catch (MalformedURLException | URISyntaxException ex) {
      log.error(ex.getMessage());
    }
  }

  @FXML
  private void openAboutWindow(ActionEvent event) {
    HelperWindows.showAboutWindow(_mainStage);
  }


}

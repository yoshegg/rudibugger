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

package de.dfki.mlt.rudibugger.view.menuBar;

import static de.dfki.mlt.rudibugger.Constants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.editor.RudibuggerEditor;
import de.dfki.mlt.rudibugger.HelperWindows;
import de.dfki.mlt.rudibugger.MainApp;
import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudibugger.view.editor.RudiTab;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
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


  /* ***************************************************************************
   * GUI ITEMS
   * **************************************************************************/

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


  @FXML
  private MenuItem openRuleLoggingWindowItem;

  @FXML
  private MenuItem openTrackingWindowItem;



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
      saveAllItem.setDisable(! projectLoaded);

      if (_model.getEditor() instanceof RudibuggerEditor)
        listenForRudibuggerEditor((RudibuggerEditor) _model.getEditor());
    });
  }

  private void listenForRudibuggerEditor(RudibuggerEditor editor) {
    editor.currentlySelectedTabProperty().addListener((obs, oct, ct) -> {
      if (oct != null) ((RudiTab) oct).hasBeenModifiedProperty()
        .removeListener(modifiedListener);
      saveAsItem.setDisable(ct == null);
      if (ct == null) return;
      RudiTab currentTab = (RudiTab) ct;
      saveItem.setDisable(! (currentTab.isKnown() && currentTab.isModified()));
      currentTab.hasBeenModifiedProperty().addListener(modifiedListener);
    });
  }

  ChangeListener<Boolean> modifiedListener
    = (o, ov, nv) -> saveItem.setDisable(! nv);



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
    openRuleLoggingWindowItem.setDisable(val);
    openTrackingWindowItem.setDisable(val);
  }

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


  /* ***************************************************************************
   * MENU ACTIONS - FILE
   * **************************************************************************/

  @FXML
  private void newProjectAction(ActionEvent event) {
    _model.createNewProject();
  }

  @FXML
  private void newRudiFileAction(ActionEvent event)
          throws FileNotFoundException {
    _model.getEditor().createNewFile();
  }


  @FXML
  private void openProjectAction(ActionEvent event)
          throws FileNotFoundException, IOException, IllegalAccessException {
    Path projectYml = HelperWindows.openYmlProjectFileDialog(_mainStage);
    if (projectYml == null) return;
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
    _model.getEditor().saveFile();
  }

  @FXML
  private void saveAsAction(ActionEvent event) {
    Path newFile = HelperWindows.openSaveNewFileAsDialog(_mainStage,
      _model.getLoadedProject().getRudiFolder());
    _model.getEditor().saveFileAs(newFile);
  }

  @FXML
  private void saveAllAction(ActionEvent event) {
    _model.getEditor().saveAllFiles();
  }


  @FXML
  private void exitAction(ActionEvent event) {
    _model.layout.saveLayoutToFile();
    MainApp.exitRudibugger();
  }


  /* ***************************************************************************
   * MENU ACTIONS - EDIT
   * **************************************************************************/

  @FXML
  private void findInProject(ActionEvent event) {
    Project project = _model.getLoadedProject();
    HelperWindows.openSearchWindow(_mainStage, _model.getEditor(),
      project.getRudiFolder());
  }


  /* ***************************************************************************
   * MENU ACTIONS - TOOLS
   * **************************************************************************/

  @FXML
  private void openSettingsDialog(ActionEvent event) {
    HelperWindows.showSettingsWindow(_mainStage, _model.globalConf);
  }

  @FXML
  private void openRuleLoggingWindow(ActionEvent event) {
    HelperWindows.showRuleLoggingWindow(_mainStage, _model.getLoadedProject(),
      _model.getEditor(), _model.globalConf);
  }

  @FXML
  private void openTrackingWindow(ActionEvent event) {
    HelperWindows.showTrackingWindow(_mainStage, _model.getEditor());
  }


  /* ***************************************************************************
   * MENU ACTIONS - HELP
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

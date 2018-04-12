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

package de.dfki.mlt.rudibugger.Controller;

import static de.dfki.mlt.rudibugger.Constants.*;

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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MenuController {

  /** the logger of the MenuController */
  static Logger log = LoggerFactory.getLogger("GUIlog");

  /** the DataModel */
  private DataModel _model;

  /** This function connects this controller to the DataModel
   *
   * @param model
   */
  public void initModel(DataModel model) {
    if (this._model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    _model = model;

    /* this listener checks for a compile file */
    _model.project.compileFileProperty().addListener((o, oldVal, newVal) -> {
      if (newVal != null) {
        log.debug("As a compile file has been found, "
                + "the button was enabled.");
        compileButton.setDisable(false);
      } else {
        compileButton.setDisable(true);
      }
    });

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
    _model.projectStatusProperty().addListener((o, oldVal, newVal) -> {
      if ((int) newVal == PROJECT_OPEN) {
        log.debug("Project open: enable GUI-elements.");
        closeProjectItem.setDisable(false);
        newRudiFileItem.setDisable(false);
        loadLoggingStateMenu.setDisable(false);
        saveLoggingStateItem.setDisable(false);
        manageVondaConnectionButton();
        defineCompileButton();
      } else if ((int) newVal == PROJECT_CLOSED) {
        log.debug("Project closed: disable GUI-elements.");
        closeProjectItem.setDisable(true);
        newRudiFileItem.setDisable(true);
        loadLoggingStateMenu.setDisable(true);
        saveLoggingStateItem.setDisable(true);
        manageVondaConnectionButton();
        defineCompileButton();
      }
    });

    _model.vonda.connectedProperty().addListener((o, ov, nv) -> {
      manageVondaConnectionButton();
    });

    /* this listener enables saving depending on the selected tab */
    _model.selectedTabProperty().addListener((o, oldVal, newVal) -> {

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

  private void manageVondaConnectionButton() {

    Button button = vondaConnectionButton;

    if (_model.projectStatusProperty().get() == PROJECT_CLOSED) {
      button.setText("No project");
      button.setOnMouseEntered(e -> button.setText(null));
      button.setOnMouseExited(e -> button.setText(null));
      button.setDisable(true);
      requestedConnection = false;

    } else if (_model.vonda.connectedProperty().get()) {
      requestedConnection = false;
      button.setText("Connected");
      System.out.println(_model.vonda.client.isConnected());
      button.setOnMouseEntered(e -> button.setText("Disconnect"));
      button.setOnMouseExited(e -> button.setText("Connected"));
      button.setDisable(false);

    } else if (! _model.vonda.connectedProperty().get()) {
      if (requestedConnection) {
        button.setText("Connecting");
        button.setOnMouseEntered(e -> button.setText("Disconnect"));
        button.setOnMouseExited(e -> button.setText("Connecting"));
        button.setDisable(false);
      } else {
        button.setText("Disconnected");
        button.setOnMouseEntered(e -> button.setText("Connect"));
        button.setOnMouseExited(e -> button.setText("Disconnected"));
        button.setDisable(false);
      }
    } else {
      log.debug("Unexpected behaviour with connection button.");
    }
  }

  private void buildLoadRuleSelectionStateMenu() {
    loadLoggingStateMenu.getItems().clear();
    _model.getRecentRuleLoggingStates().forEach((x) -> {
      MenuItem mi = new MenuItem(x.toString());
      mi.setOnAction((event) -> {
        _model.loadRuleLoggingState(x);
      });
      loadLoggingStateMenu.getItems().add(mi);
    });
  }

  /**
   * This function is used to check for open projects
   *
   * @param ymlFile null, if the project has not been defined yet, else the Path
   * to the project's .yml file
   */
  private void checkForOpenProject(Path ymlFile) {

    /* a project is already open */
    if (_model.projectStatusProperty().getValue() == PROJECT_OPEN) {
      switch (HelperWindows.overwriteProjectCheck(_model)) {
        case OVERWRITE_CHECK_CURRENT_WINDOW:
          if (ymlFile == null)
            ymlFile = HelperWindows.openYmlProjectFile(_model.stageX);
          if (ymlFile == null) return;

          _model.close(true);
          _model.init(ymlFile);

          break;
        case OVERWRITE_CHECK_NEW_WINDOW:
        //TODO: not implemented yet.
        case OVERWRITE_CHECK_CANCEL:
          break;
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

  public void defineCompileButton() {
    // TODO: Can probably be shortened.

    /* No compile file & no custom compile commands. */
    if (  (_model.project.getCompileFile() == null)
        & (_model.project.getCustomCompileCommands().isEmpty()) ) {
      if (toolBar.getItems().contains(customCompileButton)) {
        toolBar.getItems().remove(customCompileButton);
        toolBar.getItems().add(0, compileButton);
      }
    }

    /* Compile file, but no custom compile commands. */
    else if ( (_model.project.getCompileFile() != null)
            & (_model.project.getCustomCompileCommands().isEmpty()) ) {
      if (toolBar.getItems().contains(customCompileButton)) {
        toolBar.getItems().remove(customCompileButton);
        toolBar.getItems().add(0, compileButton);
      }
    }

    /* Compile file & custom compile commands. */
    else if ( (_model.project.getCompileFile() != null)
            & (! _model.project.getCustomCompileCommands().isEmpty()) ) {

      if (toolBar.getItems().contains(compileButton))
        toolBar.getItems().remove(compileButton);
      customCompileButton = new SplitMenuButton();
      customCompileButton.setText("Compile");
      customCompileButton.setOnAction(e -> {
        try {
          _model.compiler.startDefaultCompile();
        } catch (IOException | InterruptedException ex) {
          log.error(ex.toString());
        }
      });

      /* Iterate over alternative compile commands */
      for (String k : _model.project.getCustomCompileCommands().keySet()) {
        Label l = new Label(k);
        CustomMenuItem cmi = new CustomMenuItem(l);
        String cmd = _model.project.getCustomCompileCommands().get(k);
        Tooltip t = new Tooltip(cmd);
        Tooltip.install(l, t);
        cmi.setOnAction(f -> {
          try {
            _model.compiler.startCompile(cmd);
          } catch (IOException | InterruptedException ex) {
            log.error(ex.toString());
          }
        });
        customCompileButton.getItems().add(cmi);

      }
      toolBar.getItems().add(0, customCompileButton);
    }

  }


  /*****************************************************************************
   * The different GUI elements
   ****************************************************************************/

  /* the compile button */
  @FXML
  private Button compileButton;

  /** Custom compile button */
  private SplitMenuButton customCompileButton;

  /* the run button */
  @FXML
  private Button runButton;

  /** Con-/Disconnect button. */
  @FXML
  private Button vondaConnectionButton;


  /*****************************************************************************
   * Menu items actions (from menu bar)
   ****************************************************************************/

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


  /** Menu "Load logging state" */
  @FXML
  private Menu loadLoggingStateMenu;

  /** Action "Open configuration file..." */
  @FXML
  private void openRuleLoggingStateConfigurationFile(ActionEvent event) {
    _model.openRuleLoggingStateFileChooser();
  }


  /** MenuItem "Save logging state..." */
  @FXML
  private MenuItem saveLoggingStateItem;

  /** Action "Save logging state" */
  @FXML
  private void saveLoggingStateAction(ActionEvent event) {
    _model.requestSaveRuleLoggingState();
  }


  /** MenuItem "Save" */
  @FXML
  private MenuItem saveItem;

  /** Action "Save" */
  @FXML
  private void saveAction(ActionEvent event) {
    _model.rudiSave.quickSaveFile();
  }


  /** MenuItem "Save as..." */
  @FXML
  private MenuItem saveAsItem;

  /** Action "Save as..." */
  @FXML
  private void saveAsAction(ActionEvent event) {
    _model.rudiSave.saveFileAs();
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
    MainApp.exitRudibugger();
  }


  /********* Tools *********/
  @FXML
  private void openSettingsDialog(ActionEvent event) {
    _model.openSettingsDialog();
  }



  /*****************************************************************************
   * Button actions & toolBar
  *****************************************************************************/

  /** Contains buttons */
  @FXML
  private ToolBar toolBar;

  /* Clicking the compile button */
  @FXML
  private void startCompile(ActionEvent event) throws IOException,
          InterruptedException {
    _model.compiler.startDefaultCompile();
  }

  /* Clicking the run button */
  @FXML
  private void startRun(ActionEvent event) {
    log.warn("\"Run\" is not implemented yet.");
  }

  /* Establishes a connection to the VOnDA server or disconnects from it. */
  @FXML
 private void changeVondaConnectionState(ActionEvent event) {
    if (_model.vonda.connectedProperty().get() | requestedConnection) {
      _model.vonda.closeConnection();
    requestedConnection = false;
    } else {
      _model.vonda.connect();
      requestedConnection = true;
    }
    manageVondaConnectionButton();
  }

 private boolean requestedConnection = false;
}

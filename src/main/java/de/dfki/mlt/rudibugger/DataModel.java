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

import de.dfki.mlt.rudibugger.RuleModel.RuleModel;
import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.Controller.SettingsController;
import de.dfki.mlt.rudibugger.DataModelAdditions.*;
import de.dfki.mlt.rudibugger.FileTreeView.*;
import de.dfki.mlt.rudibugger.RuleTreeView.RuleModelState;
import de.dfki.mlt.rudibugger.TabManagement.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * The DataModel represents the business logic of rudibugger.
 *
 * TODO: SHRINK AND COMPLETE DOCUMENTATION.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class DataModel {

  /*****************************************************************************
   * BASIC FIELDS
   ****************************************************************************/

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("DataModel");

  /** YAML options. */
  private final DumperOptions _options = new DumperOptions() {{
    setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
  }};

  /** A YAML instance for further use of YAML. */
  public Yaml yaml = new Yaml(_options);

  /** The main stage, necessary when opening additional windows e.g. prompts. */
  public Stage stageX;


  /*****************************************************************************
   * ADDITIONS (ADDITIONAL MODULES OF DATAMODEL)
   ****************************************************************************/

  /** Provides additional functionality to interact with Emacs. */
  public EmacsConnection emacs = new EmacsConnection();

  /** Provides additional functionality to interact with VOnDA. */
  public VondaConnection vonda = new VondaConnection(this);

  /** Provides additional functionality to save .rudi files. */
  public RudiSaveManager rudiSave = new RudiSaveManager(this);

  /** Provides additional functionality to load .rudi files and rules. */
  public RudiLoadManager rudiLoad = new RudiLoadManager(this);

  /** Provides additional functionality concerning global configuration. */
  public GlobalConfiguration globalConf = new GlobalConfiguration(this);

  /** Provides additional functionality to start VOnDAs compiler. */
  public VondaCompilation compiler = new VondaCompilation(this);

  /** Provides additional functionality to track changes in the file system. */
  public WatchManager watch = new WatchManager(this);


  /*****************************************************************************
   * PROJECT ADDITIONS
   ****************************************************************************/

  /** Provides additional functionality about project specific information. */
  public ProjectManager project = new ProjectManager(this);

  /** Contains specific  information about project's rule structure. */
  public RuleModel ruleModel = new RuleModel(this);

  /** Contains specific information about the RuleModel's view state. */
  public RuleModelState ruleModelState = new RuleModelState(this);

  /**
   * Contains specific information about the involved <code>.rudi</code> folder
   * and files.
   */
  public RudiHierarchy rudiHierarchy = new RudiHierarchy(this);

  /*****************************************************************************
   * PROJECT INITIALIZER AND CLOSE METHODS
   ****************************************************************************/

  /**
   * Initializes a project.
   *
   * @param selectedProjectYml a configuration file of a project.
   */
  public void init(Path selectedProjectYml) {

    project.initConfiguration(selectedProjectYml);

    watch.initWatches();

    rudiHierarchy.init();

    if (Files.exists(project.getRuleLocationFile()))
      ruleModel.init();

    setProjectStatus(PROJECT_OPEN);

    globalConf.addToRecentProjects(selectedProjectYml);
    globalConf.setSetting("lastOpenedProject",
                       selectedProjectYml.toAbsolutePath().toString());
    log.info("Initializing done.");

    if (globalConf.getAutomaticallyConnectToVonda()) vonda.connect();
  }

  /**
   * Closes a project by nullifying the fields.
   *
   * @param stealthy
   */
  public void close(boolean stealthy) {
    log.info("Closing [" + project.getProjectName() + "]...");

    project.resetConfigurationWithLog();

    ruleModel.reset();
    rudiHierarchy.reset();
    watch.disableWatches();
    vonda.closeConnection();

    setProjectStatus(PROJECT_CLOSED);
    globalConf.setSetting("lastOpenedProject", "");
  }

  /** Starts a wizard to create a new VOnDA compatible project from scratch. */
  public void createNewProject() {
    log.info("Not implemented yet.");
    // TODO
  }

  /*****************************************************************************
   * OLD
   ****************************************************************************/


  private final IntegerProperty _compilationState
    = new SimpleIntegerProperty();
  public IntegerProperty _compilationStateProperty() {
    return _compilationState;
  }


  private final ObjectProperty<HashMap<Path, RudiTab>> openTabs
          = new SimpleObjectProperty<>();
  public ObjectProperty<HashMap<Path, RudiTab>> openTabsProperty() {
    return openTabs;
  }

  private final ObjectProperty<RudiTab> selectedTab
          = new SimpleObjectProperty<>();
  public ObjectProperty<RudiTab> selectedTabProperty() { return selectedTab; }

  private final ObjectProperty<RudiTab> requestedCloseTab
          = new SimpleObjectProperty<>();
  public ObjectProperty<RudiTab> requestedCloseTabProperty() {
    return requestedCloseTab;
  }

  private final ObjectProperty<FileAtPos> requestedFile
          = new SimpleObjectProperty<>();
  public ObjectProperty<FileAtPos> requestedFileProperty() {
    return requestedFile;
  }

  /*****************************************************************************
   * NOTIFICATIONS
   ****************************************************************************/

  /** statusBar */
  private final StringProperty statusBar = new SimpleStringProperty();
  public StringProperty statusBarProperty() { return statusBar; }


  /*****************************************************************************
   * UNDERLYING FIELDS / PROPERTIES OF THE CURRENT PROJECT AKA DATAMODEL
   ****************************************************************************/

  /** Project status */
  private final IntegerProperty _projectStatus
          = new SimpleIntegerProperty(PROJECT_CLOSED);
  public void setProjectStatus(int val) { _projectStatus.set(val); }
  public IntegerProperty projectStatusProperty() { return _projectStatus; }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/



  public void openSettingsDialog() {
    try {
      /* Load the fxml file and create a new stage for the settings dialog */
      FXMLLoader loader = new FXMLLoader(getClass()
              .getResource("/fxml/settings.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage settingsStage = new Stage();
      settingsStage.setTitle("Settings");
      settingsStage.initModality(Modality.WINDOW_MODAL);
      settingsStage.initOwner(stageX);
      Scene scene = new Scene(page);
      settingsStage.setScene(scene);

      /* Set the controller */
      SettingsController controller = loader.getController();
      controller.initModel(this);
      controller.setDialogStage(settingsStage);

      /* show the dialog */
      settingsStage.show();

    } catch (IOException e) {
      log.error(e.toString());
    }
  }

}

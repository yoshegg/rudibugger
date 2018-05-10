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

import de.dfki.mlt.rudibugger.SearchAndFind.SearchManager;
import de.dfki.mlt.rudibugger.RuleModel.RuleModel;
import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.Controller.*;
import de.dfki.mlt.rudibugger.DataModelAdditions.*;
import de.dfki.mlt.rudibugger.FileTreeView.*;
import de.dfki.mlt.rudibugger.RuleTreeView.RuleModelState;
import de.dfki.mlt.rudibugger.SearchAndFind.SearchController;
import de.dfki.mlt.rudibugger.TabManagement.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
   * PROPERTIES
   ****************************************************************************/

  /** Indicates whether or not a project has been loaded. */
  private final BooleanProperty _projectLoaded
          = new SimpleBooleanProperty(PROJECT_CLOSED);

  /** Represents the text shown on the status bar. */
  private final StringProperty statusBar
          = new SimpleStringProperty();


  /*****************************************************************************
   * ADDITIONS (ADDITIONAL MODULES OF DATAMODEL)
   ****************************************************************************/

  /** Stores information about rudibugger's layout. */
  public ViewLayout layout = new ViewLayout(this);

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

  /** Provides additional functionality to search trough files. */
  public SearchManager search = new SearchManager(this);


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

  /** Contains information about the opened tabs. */
  public TabManager tabStore = new TabManager(this);


  /*****************************************************************************
   * PROJECT INITIALIZER AND CLOSE METHODS
   ****************************************************************************/

  /**
   * Initializes a project.
   *
   * @param selectedProjectYml a configuration file of a project.
   */
  public void init(Path selectedProjectYml) {

    /** Loads project configuration and initializes fields */
    project.initConfiguration(selectedProjectYml);

    /** Start WatchServices */
    watch.initWatches();

    /** Reads in .rudi files in rudiFolder */
    rudiHierarchy.init();

    /** Reads in ruleModel (if it exists) */
    if (Files.exists(project.getRuleLocationFile()))
      ruleModel.init();

    /** Sets the property to true */
    _projectLoaded.set(PROJECT_OPEN);

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

    rudiHierarchy.reset();
    ruleModel.reset();
    watch.disableWatches();
    vonda.closeConnection();

    _projectLoaded.set(PROJECT_CLOSED);

    log.info("Project closed.");
  }

  /** Starts a wizard to create a new VOnDA compatible project from scratch. */
  public void createNewProject() {
    log.info("Not implemented yet.");
    // TODO
  }


  /*****************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS AND PROPERTIES
   ****************************************************************************/

  /** @return Property indicating whether or not a project has been loaded. */
  public BooleanProperty projectLoadedProperty() { return _projectLoaded; }

  /** @return Property representing the text shown on the statusBar. */
  public StringProperty statusBarTextProperty() { return statusBar; }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Opens the about window. */
  public void openAboutWindow() {
    try {
      /* Load the fxml file and create a new stage for the about window */
      FXMLLoader loader = new FXMLLoader(getClass()
              .getResource("/fxml/about.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage aboutStage = new Stage();
      aboutStage.setResizable(false);
      aboutStage.setTitle("About Rudibugger");
      aboutStage.initModality(Modality.NONE);
      aboutStage.initOwner(stageX);
      Scene scene = new Scene(page);
      aboutStage.setScene(scene);

      /* Set the controller */
      AboutController controller = loader.getController();
      controller.initModel(this);
      controller.setDialogStage(aboutStage);

      /* show the dialog */
      aboutStage.show();

    } catch (IOException e) {
      log.error(e.toString());
    }
  }


  /** Opens the settings menu. */
  public void openSettingsDialog() {
    try {
      /* Load the fxml file and create a new stage for the settings dialog */
      FXMLLoader loader = new FXMLLoader(getClass()
              .getResource("/fxml/settings.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage settingsStage = new Stage();
      settingsStage.setTitle("Settings");
      settingsStage.initModality(Modality.NONE);
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

  public void openSearchWindow() {
    try {
      /* Load the fxml file and create a new stage for the about window */
      FXMLLoader loader = new FXMLLoader(getClass()
        .getResource("/fxml/findInProjectWindow.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage findStage = new Stage();
      findStage.setTitle("Find in project...");
      findStage.initModality(Modality.NONE);
      findStage.initOwner(stageX);
      Scene scene = new Scene(page);
      findStage.setScene(scene);

      /* Set the controller */
      SearchController controller = loader.getController();
      controller.initModel(this);

      /* show the dialog */
      findStage.show();

    } catch (IOException e) {
      log.error(e.toString());
    }
  }

}

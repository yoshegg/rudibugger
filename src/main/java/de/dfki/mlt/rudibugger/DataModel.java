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

import de.dfki.mlt.rudibugger.Project.Project;
import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.DataModelAdditions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.property.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * TODO
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
  public Stage mainStage;


  /*****************************************************************************
   * PROPERTIES
   ****************************************************************************/

  /** Represents the text shown on the status bar. */
  private final StringProperty statusBar
          = new SimpleStringProperty();


  /*****************************************************************************
   * ADDITIONS (ADDITIONAL MODULES OF DATAMODEL)
   ****************************************************************************/

  /** Stores information about rudibugger's layout. */
  public ViewLayout layout = new ViewLayout(this);

  /** Provides additional functionality to interact with Emacs. */
  public EmacsConnection emacs = new EmacsConnection(this);

  /** Provides additional functionality concerning global configuration. */
  public GlobalConfiguration globalConf = new GlobalConfiguration(this);


  /*****************************************************************************
   * DATAMODEL CONSTRUCTOR AND INITIALIZER
   ****************************************************************************/

  /** Creates a new <code>DataModel</code>. */
  public DataModel() {
    initializeConnectionsBetweenAdditions();
  }

  /** Called to initialize connections between additions. */
  private void initializeConnectionsBetweenAdditions() {
    _project.rudiSave.initSaveListener();
  }



  public void openProject(Path projectYamlPath) {
    Project.openProject(projectYamlPath);
  }

  public void closeProject() {
    Project.closeProject();
  }



  /*****************************************************************************
   * PROJECT INITIALIZER AND CLOSE METHODS
   ****************************************************************************/

  /**
   * Initializes a project.
   *
   * @param selectedProjectYml a configuration file of a project.
   */
  public void init(Path selectedProjectYml) {

    /* Loads project configuration and initializes fields */
//    _project.initConfiguration(selectedProjectYml);

    /* Start WatchServices */
    _project.watchServices.initWatches();

    /* Reads in .rudi files in rudiFolder */
    _project.rudiHierarchy.init();

    /* Reads in ruleModel (if it exists) */
    if (Files.exists(_project.getRuleLocationFile()))
      _project.ruleModel.init();

    /* Sets the property to true */
    _projectLoaded.set(PROJECT_OPEN);

    log.info("Initializing done.");

    /* Automatically connect to VOnDA (if specified in settings) */
    if (globalConf.getAutomaticallyConnectToVonda()) _project.vonda.connect();
  }

  /**
   * Closes a project by nullifying the fields.
   *
   * @param stealthy
   */
  public void close(boolean stealthy) {
    log.info("Closing [" + _project.getProjectName() + "]...");

//    _project.resetConfigurationWithLog();
//
//    _project.rudiHierarchy.reset();
//    _project.ruleModel.reset();
    _project.watchServices.disableWatches();
//    _project.vonda.closeConnection();

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

  /** @return Property representing the text shown on the statusBar. */
  public StringProperty statusBarTextProperty() { return statusBar; }

}

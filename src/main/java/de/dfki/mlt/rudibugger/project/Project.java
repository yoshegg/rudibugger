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

package de.dfki.mlt.rudibugger.project;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.GlobalConfiguration;
import de.dfki.mlt.rudibugger.view.fileTreeView.RudiHierarchy;
import de.dfki.mlt.rudibugger.project.ruleModel.RuleModel;
import de.dfki.mlt.rudibugger.project.watchServices.RudiFolderWatch;
import de.dfki.mlt.rudibugger.project.watchServices.RuleLocationYamlWatch;
import static de.dfki.mlt.rudimant.common.Constants.*;
import de.dfki.mlt.rudimant.common.SimpleServer;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * The <code>Project</code> represents
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Project {

  static Logger log = LoggerFactory.getLogger("Project");

  private static final Yaml YAML = new Yaml(
    new DumperOptions() {{
      setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }}
  );


  /* ***************************************************************************
   * BASIC FIELDS
   * extracted from configuration .yml file or derived
   * **************************************************************************/

  /** TODO */
  private final GlobalConfiguration _globalConf;

  /** TODO */
  private final Path _projectYamlPath;

  /** Indicates the project's name. */
  private final String _projectName;

  /** Represents the project's RuleLoc.yml. */
  private final Path _ruleLocYaml;

  /** Represents the path to the project's RuleModelStates' save folder. */
  private final Path _ruleModelStatesFolder;

  /** Represents the project's root folder. */
  private final Path _rootFolder;

  /** Represents the project's .rudi folder. */
  private final Path _rudiFolder;

  /** Contains all project configuration data. */
  private ObservableMap<String, Object> _projectConfigs;


  /* ***************************************************************************
   * SOPHISTICATED FIELDS
   * **************************************************************************/

  /**
   * Contains specific information about the involved <code>.rudi</code> folder
   * and files.
   */
  private final RudiHierarchy _rudiHierarchy;

  /** Represents the project's rule structure. */
  private final ObjectProperty<RuleModel> _ruleModel
          = new SimpleObjectProperty<>(null);

  /** Represents the connection to VOnDA's runtime system. */
  public VondaRuntimeConnection vonda;

//  /** Contains information about the opened tabs. */
//  private final TabStore _tabStore = new TabStore();

  /** Represents VOnDAs compiler. */
  public VondaCompiler compiler = new VondaCompiler(this);

  /** Watches the .rudi folder for changes. */
  private final RudiFolderWatch _rudiFolderWatch;

  /** Watches the RuleLoc.yml file for changes. */
  private final RuleLocationYamlWatch _ruleLocYamlWatch;



  /* ***************************************************************************
   * OPEN, CLOSE, CONSTRUCTOR
   * **************************************************************************/

  /** Opens a new project. TODO: Complete */
  public static Project openProject(Path projectYamlPath,
          GlobalConfiguration globalConf) {
    Project project;
    try {
      project = new Project(projectYamlPath, globalConf);
    } catch (IOException | IllegalArgumentException e) {
      log.error(e.toString());
      return null;
    }
    return project;
  }

  /** Closes the project (if any). */
  public void closeProject() {
    this.vonda.closeConnection();
    this._rudiFolderWatch.shutDownListener();
    this._ruleLocYamlWatch.shutDownListener();
  }

  private Project(Path projectYamlPath, GlobalConfiguration globalConf)
          throws IOException {
    _globalConf = globalConf;
    _projectYamlPath = projectYamlPath;
    Map configMap = readInProjectConfigurationYaml();
    checkConfigurationForValidity(configMap);
    _projectConfigs = FXCollections.observableMap(configMap);
    _projectName = identifyProjectName();
    _rootFolder = _projectYamlPath.getParent();
    _rudiFolder = retrieveRudiFolder();
    createPotentiallyMissingFolders();
    _ruleLocYaml = retrieveRuleLocYaml();
    _ruleModelStatesFolder = retrieveRuleModelStatesFolder();

    _rudiHierarchy = new RudiHierarchy(_rudiFolder, _ruleLocYaml);
    _rudiFolderWatch = RudiFolderWatch.createRudiFolderWatch(
          _rudiHierarchy, _rudiFolder);
    _ruleLocYamlWatch = RuleLocationYamlWatch.createRuleLocationWatch(
            this, _rudiHierarchy, getGeneratedFilesFolder());
    initCompileCommands();
    enableListeners();

    if (Files.exists(_ruleLocYaml)) {
      initRuleModel();
      vonda = new VondaRuntimeConnection(_ruleModel.get());
    }
  }

  public void initRuleModel() {
    RuleModel rm = RuleModel.createRuleModel(_rudiFolder, _ruleLocYaml);
    _ruleModel.set(rm);
  }


  /* ***************************************************************************
   * LOAD PROJECT
   * **************************************************************************/

  private static final String ABORT_MESSAGE =
      "Aborted initializing of project fields.";

  private static final String CONTINUE_MESSAGE =
      "Continuing initiliazing of project fields.";

  /** Contains the keys a default project configuration file. */
  private static final HashSet<String> DEFAULT_PROJECT_CONFIGURATION_KEYS =
          new HashSet<String>() {{
      add("outputDirectory");
      add("wrapperClass");
      add("ontologyFile");
      add("rootPackage");
    }};

  private Map readInProjectConfigurationYaml() throws IOException {
    log.debug("Reading in project's configuration .yml...");
    Map map = (HashMap<String, Object>) YAML.load(
        new FileInputStream(_projectYamlPath.toFile()));
    return map;
  }

  /** Checks if a read in map represents a project configuration. */
  private void checkConfigurationForValidity(Map loadedConfig)
          throws IllegalArgumentException {
    log.debug("Checking configuration for validity...");
    if (! loadedConfig.keySet()
            .containsAll(DEFAULT_PROJECT_CONFIGURATION_KEYS)) {
      ArrayList<String> missingKeys = new ArrayList<>();
      for (String key: DEFAULT_PROJECT_CONFIGURATION_KEYS) {
        if (! loadedConfig.containsKey(key))
          missingKeys.add(key);
      }
      String errMessage
              = ("Config file is missing the following key(s): " + missingKeys);
      throw new IllegalArgumentException(errMessage);
    }
  }

  private String identifyProjectName() {
    String filename = _projectYamlPath.getFileName().toString();
    return filename.substring(0, filename.lastIndexOf('.'));
  }

  private Path retrieveRudiFolder() throws IOException {
    Path rudiFolder = _rootFolder.resolve(PATH_TO_RUDI_FOLDER);
    if (! Files.exists(rudiFolder)) {
      String errorMessage = ".rudi folder could not be found. \n"
              + "Should be here: " + _rudiFolder.toString() + "\n"
              + ABORT_MESSAGE;
      throw new IOException(errorMessage);
    }
    return rudiFolder;
  }

  private Path retrieveRuleLocYaml() {
    Path ruleLocYaml = _rootFolder.resolve(getGeneratedFilesFolder()
      .resolve(RULE_LOCATION_FILE));
    if (! Files.exists(ruleLocYaml)) {
      log.info("RuleLoc.yml could not be found. \n"
             + "Could be here: " + _ruleLocYaml
             + "; but was probably not compiled yet. \n"
             + CONTINUE_MESSAGE);
    }
    return ruleLocYaml;
  }

  private Path retrieveRuleModelStatesFolder() {
    Path ruleModelStatesFolder = GLOBAL_CONFIG_PATH
      .resolve("loggingConfigurations").resolve(_projectName);
    if (! Files.exists(ruleModelStatesFolder)) {
      ruleModelStatesFolder.toFile().mkdirs();
      log.debug("Created " + ruleModelStatesFolder);
    }
    return ruleModelStatesFolder;
  }

  private void createPotentiallyMissingFolders() {
    /* Create generated directory (if necessary) */
    Path generatedFilesFolder = _rootFolder.resolve(getGeneratedFilesFolder());
    if (! Files.exists(generatedFilesFolder)) {
      generatedFilesFolder.toFile().mkdirs();
      log.debug("Created " + generatedFilesFolder);
    }

    /* Create output directory (if necessary) */
    Path generatedJavaFolder = _rootFolder.resolve(getGeneratedJavaFolder());
    if (! Files.exists(generatedJavaFolder)) {
      generatedJavaFolder.toFile().mkdirs();
      log.debug("Created " + generatedJavaFolder);
    }
  }


  /* ***************************************************************************
   * SAVE PROJECT'S CONFIGURATION
   * **************************************************************************/

  /** Saves the project's configuration. */
  private void saveProjectConfiguration() {
    try {
      FileWriter writer = new FileWriter(_projectYamlPath.toFile());
      YAML.dump(_projectConfigs, writer);
    } catch (IOException ex) {
       log.error("Could not save project configuration file.");
    }
  }


  /* ***************************************************************************
   * GETTERS
   * **************************************************************************/

  /** @return project's configuration .yml file */
  public Path getConfigurationYml() { return _projectYamlPath; }

  /** @return The project's name */
  public String getProjectName() { return _projectName; }

  /** @return The project's root folder */
  public Path getRootFolder() { return _rootFolder; }

  /** @return The project's .rudi folder */
  public Path getRudiFolder() { return _rudiFolder; }

  /**
   * @return The project's generated directory
   * (usually src/main/resources/generated)
   */
  public Path getGeneratedFilesFolder() {
    return _rootFolder.resolve(PATH_TO_GENERATED_FOLDER);
  }

  /** @return The project's output directory (aka gen-java) */
  public Path getGeneratedJavaFolder() {
    String c = (String) _projectConfigs.get("outputDirectory");
    return Paths.get(c);
  }

  /** @return The Path to the RuleLoc.yml */
  public Path getRuleLocationFile() {return _ruleLocYaml; }

  /** @return The Path to the RuleModelStates' save folder */
  public Path getRuleModelStatesFolder() { return _ruleModelStatesFolder; }

  /**
   * Temporarily shows a message on the statusBar.
   *
   * @param file the file that has been saved
   */
  private void notifySaved(String file) {
//    _model.statusBarTextProperty().set("Saved " + file + ".");
//    PauseTransition pause = new PauseTransition(Duration.seconds(3));
//    pause.setOnFinished(Ce -> _model.statusBarTextProperty().set(null));
//    pause.play();
  }


  /*****************************************************************************
   * FIELDS AND PROPERTIES
   ****************************************************************************/

  private Map<String, String> compileCommands;

  /** Represents the default compile command.
   *  TODO: WHO LISTENS FOR THIS?
   */
  private final StringProperty _defaultCompileCommand
          = new SimpleStringProperty("");


  /*****************************************************************************
   * PROJECT CONFIGURATION METHODS
   ****************************************************************************/

  private void initCompileCommands() {
    Path compileScript = _rootFolder.resolve(COMPILE_FILE);
    if (Files.exists(compileScript)) {
      compileCommands = new LinkedHashMap<>();
      compileCommands.put("Compile", compileScript.toAbsolutePath().toString());
    }
    compileCommands.putAll(getCustomCompileCommands());

    if (_projectConfigs.containsKey("defaultCompileCommand")) {
      _defaultCompileCommand.set((String) _projectConfigs
              .get("defaultCompileCommand"));
    }
  }



  /*****************************************************************************
   * UPDATING
   ****************************************************************************/

  /**
   * Defines listeners to automatically update relative files and properties on
   * changes.
   */
  private void enableListeners() {
    _defaultCompileCommand.addListener(defaultCompileCommandListener);
  }

  /** Disables listeners to update project's config file. */
  private void disableListeners() {
    _defaultCompileCommand.removeListener(defaultCompileCommandListener);
  }

  private final ChangeListener<String> defaultCompileCommandListener
          = (o, ov, nv) -> {
            _projectConfigs.put("defaultCompileCommand", nv);
            saveProjectConfiguration();
          };


  /*****************************************************************************
   * OTHER METHODS
   ****************************************************************************/

  /** Represents a list of all recent <code>RuleTreeViewState</code>s. */
  private List<Path> _recentStates = new ArrayList<>();

  /**
   * Retrieves the 10 most recent saved RuleTreeViewState configurations.
   */
  public void retrieveRecentConfigurations() {
    List<Path> temp = new ArrayList<>();

    /* Retrieve all files and add them to a list. */
    Stream<Path> stream;
    try {
      stream = Files.walk(_ruleModelStatesFolder);
    } catch (IOException e) {
      log.error(e.toString());
      return;
    }
    stream.forEach(x -> { if (!Files.isDirectory(x)) temp.add(x); });

    /* Sort the list by modification date. */
    Collections.sort(temp, (Path p1, Path p2) ->
        Long.compare(p2.toFile().lastModified(), p1.toFile().lastModified()));

    /* Set the last 10 as the one's that will be shown in the menu. */
    int subListLength = 10;
    if (temp.size() < 10) subListLength = temp.size();
    _recentStates = temp.subList(0, subListLength);
  }

  /** @return A list of all recently saved <code>RuleTreeViewState</code>s */
  public List<Path> getRecentStates() {
    retrieveRecentConfigurations();
    return _recentStates;
  }

  /* ***************************************************************************
   * GETTERS FOR FIELDS
   * **************************************************************************/

  public ObjectProperty<RuleModel> ruleModelProperty() { return _ruleModel; }
  public RuleModel getRuleModel() { return _ruleModel.get(); }
  public RudiHierarchy getRudiHierarchy() { return _rudiHierarchy; }
//  public TabStore getTabStore() { return _tabStore; }


  /*****************************************************************************
   * GETTERS REPRESENTING CONFIGURATION DETAILS
   ****************************************************************************/

  /** @return The default compile command */
  public void setDefaultCompileCommand(String label) {
    _defaultCompileCommand.set(label);
  }

  public Collection<String> getCompileCommandLabels() {
    return compileCommands.keySet();
  }

  /** @return The default compile command label */
  public String getDefaultCompileCommand() {
    String result = _defaultCompileCommand.get();
    if (result == null || result.isEmpty()) {
      Collection<String> ccommands = getCompileCommandLabels();
      if (! ccommands.isEmpty()) {
        result = ccommands.iterator().next();
      }
    }
    return result;
  }

  /** @return the real command to execute for a compile command label */
  public String getCompileCommand(String label) {
    return compileCommands.get(label);
  }



  /** @return The project's wrapper class */
  public Path getWrapperClass() {
    String longName = (String) _projectConfigs.get("wrapperClass");
    String[] split = longName.split("\\.");
    String shortName = split[split.length-1];
    return _rudiFolder.resolve(shortName + RULE_FILE_EXTENSION);
  }

  /** @return The project's ontology */
  public Path getOntology() {
    String s = (String) _projectConfigs.get("ontologyFile");
    return Paths.get(s);
  }

  /** @return The custom VOnDA port (if any) or the default port */
  public int getVondaPort() {
    if (_projectConfigs.containsKey("vondaPort"))
      return (int) _projectConfigs.get("vondaPort");
    else
      return SimpleServer.DEFAULT_PORT;
  }

  /**
   * @return A Map of custom compile commands (if any) with the name of the
   * command as key and the command itself as value
   */
  private LinkedHashMap<String, String> getCustomCompileCommands() {
    if (_projectConfigs.containsKey("customCompileCommands")) {
      return (LinkedHashMap<String, String>) _projectConfigs
        .get("customCompileCommands");
    } else {
      return new LinkedHashMap<>();
    }
  }

}

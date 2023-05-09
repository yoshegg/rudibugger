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

import static de.dfki.mlt.rudibugger.ConfigurationConstants.CUSTOM_COMPILE_COMMANDS;
import static de.dfki.mlt.rudibugger.ConfigurationConstants.DEFAULT_COMPILE_COMMAND;
import static de.dfki.mlt.rudibugger.Constants.GLOBAL_PROJECT_SPECIFIC_CONFIG_PATH;
import static de.dfki.mlt.rudibugger.Constants.PATH_TO_GENERATED_FOLDER;
import static de.dfki.mlt.rudibugger.Constants.PATH_TO_RUDI_FOLDER;
import static de.dfki.mlt.rudibugger.Constants.PROJECT_SPECIFIC_RUDIBUGGER_CONFIG_FILE;
import static de.dfki.mlt.rudimant.common.Constants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import de.dfki.mlt.rudibugger.project.ruleModel.RuleModel;
import de.dfki.mlt.rudibugger.project.watchServices.RudiFolderWatch;
import de.dfki.mlt.rudibugger.project.watchServices.RuleLocationYamlWatch;
import de.dfki.mlt.rudibugger.view.fileTreeView.RudiHierarchy;
import de.dfki.mlt.rudimant.common.SimpleServer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

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

  /** Represents the file containing the project's configuration. */
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

  /** Represents the file containing project specific configs for rudibugger. */
  private Path _rudibuggerSpecificConfigsPath;

  /** Contains all project configuration data. */
  private final ObservableMap<String, Object> _projectConfigs;

  /** Contains project specific settings for rudibugger. */
  private final ObservableMap<String, Object> _rudibuggerSpecificConfigs;


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

  /** Represents VOnDAs compiler. */
  public VondaCompiler compiler;

  /** Watches the .rudi folder for changes. */
  private RudiFolderWatch _rudiFolderWatch;

  /** Watches the RuleLoc.yml file for changes. */
  private RuleLocationYamlWatch _ruleLocYamlWatch;


  /* ***************************************************************************
   * OPEN, CLOSE, CONSTRUCTOR
   * **************************************************************************/

  /** Opens a new project. */
  public static Project openProject(Path projectYamlPath) {
    try {
      return new Project(projectYamlPath);
    } catch (IOException | IllegalArgumentException e) {
      log.error(e.toString());
      return null;
    }
  }

  /** Closes the project (if any). */
  public void closeProject() {
    disableListeners();
    vonda.closeConnection();
    _rudiFolderWatch.shutDownListener();
    _ruleLocYamlWatch.shutDownListener();
  }

  /** Creates a new instance of this class. */
  private Project(Path projectYamlPath) throws IOException {
    _projectYamlPath = projectYamlPath;
    Map configMap = readInProjectConfigurationYaml();
    checkConfigurationForValidity(configMap);
    _projectConfigs = FXCollections.observableMap(configMap);
    _projectName = identifyProjectName();
    _rootFolder = _projectYamlPath.getParent();
    _rudiFolder = retrieveRudiFolder((String)configMap.get(CFG_INPUT_FILE));
    createPotentiallyMissingFolders();
    _ruleLocYaml = retrieveRuleLocYaml();
    _ruleModelStatesFolder = retrieveRuleModelStatesFolder();
    Map rudibuggerSpecificConfigMap
            = readInRudibuggerSpecificConfigurationYaml();
    _rudibuggerSpecificConfigs
            = FXCollections.observableMap(rudibuggerSpecificConfigMap);

    _rudiHierarchy = new RudiHierarchy(_rudiFolder, _ruleLocYaml);
    initWatches();
    compiler = new VondaCompiler(this);
    enableListeners();

    if (Files.exists(_ruleLocYaml)) {
      initRuleModel();
    }
  }

  public final void initRuleModel() {
    RuleModel rm = RuleModel.createRuleModel(_rudiFolder, _ruleLocYaml);
    vonda = new VondaRuntimeConnection(rm);
    _ruleModel.set(rm);
  }

  private void initWatches() {
    _rudiFolderWatch = RudiFolderWatch.createRudiFolderWatch(
          _rudiHierarchy, _rudiFolder);
    _ruleLocYamlWatch = RuleLocationYamlWatch.createRuleLocationWatch(
            this, _rudiHierarchy, getGeneratedFilesFolder());
  }


  /* ***************************************************************************
   * LOAD PROJECT
   * **************************************************************************/

  private static final String ABORT_MESSAGE =
      "Aborted initializing of project fields.";

  private static final String CONTINUE_MESSAGE =
      "Continuing initializing of project fields.";

  /** Contains the keys a default project configuration file. */
  private static final HashSet<String> DEFAULT_PROJECT_CONFIGURATION_KEYS =
          new HashSet<String>() {{
      add(CFG_INPUT_FILE);
      add(CFG_OUTPUT_DIRECTORY);
      add(CFG_ONTOLOGY_FILE);
      add(CFG_PACKAGE);
      //add(CFG_AGENT_BASE_CLASS); // optional since VOnDA 3.0
    }};

  /** Reads in the project's configuration .yml file. */
  private Map<String, Object> readInProjectConfigurationYaml() throws IOException {
    log.debug("Reading in project's configuration .yml...");
    Map<String, Object> map = (Map<String, Object>) YAML.load(
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

  private Map<String, Object> readInRudibuggerSpecificConfigurationYaml() throws IOException {
    log.debug("Reading in specific rudibugger settings of the project...");
    _rudibuggerSpecificConfigsPath = GLOBAL_PROJECT_SPECIFIC_CONFIG_PATH
            .resolve(_projectName)
            .resolve(PROJECT_SPECIFIC_RUDIBUGGER_CONFIG_FILE);
    if (! Files.exists(_rudibuggerSpecificConfigsPath)) {
      log.info("No project specific settings found. Creating file...");
      _rudibuggerSpecificConfigsPath.getParent().toFile().mkdirs();
      _rudibuggerSpecificConfigsPath.toFile().createNewFile();
    }
    Map<String, Object> map = (Map<String, Object>) YAML.load(new FileInputStream(
          _rudibuggerSpecificConfigsPath.toFile()));
    if (map == null) map = new HashMap<>();
    return map;
  }

  /** @return The project's name from the configuration file. */
  private String identifyProjectName() {
    String filename = _projectYamlPath.getFileName().toString();
    return filename.substring(0, filename.lastIndexOf('.'));
  }

  /** @return The project's .rudi folder. */
  private Path retrieveRudiFolder(String inputFileName) throws IOException {
    File rudiDir = new File(inputFileName).getParentFile();
    Path rudiFolder = rudiDir == null
        ? _rootFolder : _rootFolder.resolve(rudiDir.toPath());
    if (! Files.exists(rudiFolder)) {
      String errorMessage = ".rudi folder could not be found. \n"
              + "Should be here: " + _rudiFolder.toString() + "\n"
              + ABORT_MESSAGE;
      throw new IOException(errorMessage);
    }
    return rudiFolder;
  }

  /** @return The project's RuleLoc.yml path. */
  private Path retrieveRuleLocYaml() {
    Path ruleLocYaml = getGeneratedFilesFolder()
      .resolve(RULE_LOCATION_FILE);
    if (! Files.exists(ruleLocYaml)) {
      log.info("RuleLoc.yml could not be found. \n"
             + "Could be here: " + _ruleLocYaml
             + "; but was probably not compiled yet. \n"
             + CONTINUE_MESSAGE);
    }
    return ruleLocYaml;
  }

  /** @return The Project's folder containing rule logging configurations. */
  private Path retrieveRuleModelStatesFolder() {
    Path ruleModelStatesFolder = GLOBAL_PROJECT_SPECIFIC_CONFIG_PATH
      .resolve(_projectName).resolve("loggingConfigurations");
    if (! Files.exists(ruleModelStatesFolder)) {
      ruleModelStatesFolder.toFile().mkdirs();
      log.debug("Created " + ruleModelStatesFolder);
    }
    return ruleModelStatesFolder;
  }

  /**
   * If needed, the folder containing generated files (like RuleLoc.yml) and
   * the folder containing the compiled .java files will be created.
   */
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
   * SAVE PROJECT PROJECT SPECIFIC RUDIBUGGER CONFIGURATION
   * **************************************************************************/

  /** Saves the project's configuration. */
  private void saveRudibuggerSpecificProjectConfiguration() {
    try {
      FileWriter writer = new FileWriter(
              _rudibuggerSpecificConfigsPath.toFile());
      YAML.dump(_rudibuggerSpecificConfigs, writer);
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
  public final Path getGeneratedFilesFolder() {
    return _rootFolder.resolve(PATH_TO_GENERATED_FOLDER);
  }

  /** @return The project's output directory (aka gen-java) */
  public Path getGeneratedJavaFolder() {
    String c = (String) _projectConfigs.get(CFG_OUTPUT_DIRECTORY);
    return Paths.get(c);
  }

  /** @return The Path to the RuleLoc.yml */
  public Path getRuleLocationFile() {return _ruleLocYaml; }

  /** @return The Path to the RuleModelStates' save folder */
  public Path getRuleModelStatesFolder() { return _ruleModelStatesFolder; }

  /**
   * @return The Path to the config file containing project specific rudibugger
   * settings.
   */
  public Path getRudibuggerSpecificConfigYml() {
    return _rudibuggerSpecificConfigsPath;
  }

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


  /* ***************************************************************************
   * UPDATING
   * **************************************************************************/

  /**
   * Defines listeners to automatically update relative files and properties on
   * changes.
   */
  private void enableListeners() {
    _rudibuggerSpecificConfigs.addListener(rudibuggerSpecificProjectConfigurationListener);
  }

  /** Disables listeners to update project's config file. */
  private void disableListeners() {
    _rudibuggerSpecificConfigs.removeListener(rudibuggerSpecificProjectConfigurationListener);
  }

  private final MapChangeListener<String, Object>
          rudibuggerSpecificProjectConfigurationListener
          = (mcl) -> saveRudibuggerSpecificProjectConfiguration();


  /* ***************************************************************************
   * OTHER METHODS
   * **************************************************************************/

  /** Represents a list of all recent <code>RuleTreeViewState</code>s. */
  private List<Path> _recentStates = new ArrayList<>();

  /** Retrieves the 10 most recent saved RuleTreeViewState configurations. */
  public void retrieveRecentConfigurations() {
    List<Path> temp = new ArrayList<>();

    /* Retrieve all files and add them to a list. */
    try (Stream<Path> stream = Files.walk(_ruleModelStatesFolder)) {
      stream.forEach(x -> { if (!Files.isDirectory(x)) temp.add(x); });
    } catch (IOException e) {
      log.error(e.toString());
      return;
    }

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


  /* ***************************************************************************
   * GETTERS REPRESENTING CONFIGURATION DETAILS
   * **************************************************************************/

//  /** @return The default compile command */
//  public void setDefaultCompileCommand(String label) {
//    _defaultCompileCommand.set(label);
//  }

//  /** @return The default compile command label */
//  public String getDefaultCompileCommand() {
//    String result = _defaultCompileCommand.get();
//    if (result == null || result.isEmpty()) {
//      Collection<String> ccommands = getCompileCommandLabels();
//      if (! ccommands.isEmpty()) result = ccommands.iterator().next();
//    }
//    return result;
//  }

  /** @return The project's agent base class, if any. Otherwise, return null.
   *  agentBase, formerly wrapperClass, is optional since VOnDA 3.0
   */
  public Path getOptionalAgentBaseClass() {
    String longName = (String) _projectConfigs.get(CFG_AGENT_BASE_CLASS);
    if (longName == null) return null;
    String[] split = longName.split("\\.");
    String shortName = split[split.length-1];
    return _rudiFolder.resolve(shortName + RULE_FILE_EXTENSION);
  }

  /** @return The project's ontology */
  public Path getOntology() {
    String s = (String) _projectConfigs.get(CFG_ONTOLOGY_FILE);
    return Paths.get(s);
  }

  /** @return The custom VOnDA port (if any) or the default port */
  public int getVondaPort() {
    if (_projectConfigs.containsKey(CFG_DEBUG_PORT))
      return (int) _projectConfigs.get(CFG_DEBUG_PORT);
    else
      return SimpleServer.DEFAULT_PORT;
  }

  /**
   * @return A Map of custom compile commands (if any) with the name of the
   * command as key and the command itself as value
   */
  public LinkedHashMap<String, String> getCustomCompileCommands() {
    if (_projectConfigs.containsKey(CUSTOM_COMPILE_COMMANDS))
      return (LinkedHashMap<String, String>) _projectConfigs
        .get(CUSTOM_COMPILE_COMMANDS);
    else
      return new LinkedHashMap<>();
  }

  public String getDefaultCompileCommand() {
    if (_rudibuggerSpecificConfigs.keySet().contains(DEFAULT_COMPILE_COMMAND))
      return (String) _rudibuggerSpecificConfigs.get(DEFAULT_COMPILE_COMMAND);
    else
      return null;
  }

  public void setDefaultCompileCommand(String defCom) {
    _rudibuggerSpecificConfigs.put("defaultCompileCommand", defCom);
  }

}

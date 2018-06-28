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

package de.dfki.mlt.rudibugger.DataModelAdditions;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.DataModel;
import static de.dfki.mlt.rudibugger.HelperMethods.slice_end;
import static de.dfki.mlt.rudimant.common.Constants.*;
import de.dfki.mlt.rudimant.common.SimpleServer;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality about project specific information and
 * functionality.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ProjectManager {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("Project");

  /** The <code>DataModel</code> */
  private final DataModel _model;

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public ProjectManager(DataModel model) {
    _model = model;
  }

  /*****************************************************************************
   * FIELDS AND PROPERTIES
   ****************************************************************************/

  /** Contains all project configuration data. */
  private ObservableMap<String, Object> _projectConfigs;

  /** Represents the project's configuration .yml. */
  private Path _configurationYml;

  /** Contains the keys a default project configuration file. */
  private static final HashSet<String> DEFAULT_PROJECT_CONFIGURATION_KEYS =
          new HashSet<String>() {{
      add("outputDirectory");
      add("wrapperClass");
      add("ontologyFile");
      add("rootPackage");
    }};

  /** Indicates the project's name. */
  private final StringProperty projectName = new SimpleStringProperty();

  /** Represents the file containing the compile file. */
  private final ObjectProperty<Path> compileFile
    = new SimpleObjectProperty<>(null);

  /** Represents the default compile command. */
  private final StringProperty _defaultCompileCommand
          = new SimpleStringProperty("");

  /** Represents the project's RuleLoc.yml. */
  private Path ruleLocationFile = null;

  /** Represents the path to the project's RuleModelStates' save folder. */
  private Path _ruleModelStatesFolder;

  /** Represents the project's root folder. */
  private Path rootFolder = null;

  /** Represents the project's .rudi folder. */
  private Path rudiFolder = null;


  /*****************************************************************************
   * PROJECT CONFIGURATION METHODS
   ****************************************************************************/

  /**
   * Initializes a project's configuration using a provided configuration file.
   *
   * @param selectedProjectYml The selected project configuration .yml
   * @return True, if everything went well, else false
   */
  public boolean initConfiguration(Path selectedProjectYml) {

    while (true) {

      log.info("Loading project configuration...");
      if (! loadProjectConfiguration(selectedProjectYml)) break;

      log.info("Initializing project fields...");
      if (! initFields()) break;
      else log.info("Successfully initialized project fields.");

      enableListeners();
      return true;
    }

    resetConfigurationWithoutLog();
    return false;

  }

  /**
   * Called if a project has not been completely initialized. Every field will
   * then be nullified.
   */
  private void resetConfigurationWithoutLog() { resetConfiguration(true); }

  /**
   * Called if a project has been closed. Every field will then be nullified.
   */
  public void resetConfigurationWithLog() { resetConfiguration(false); }

  /**
   * Called if a project has been closed or could not even be initialized. In
   * this case, every field will be nullified.
   *
   * @param stealthy If true, no logs will be produced.
   */
  private void resetConfiguration(boolean stealthy) {
    _projectConfigs.clear();
    ruleLocationFile = null;
    rudiFolder = null;
    rootFolder = null;
    projectName.set(null);
    compileFile.set(null);
    _configurationYml = null;
    _defaultCompileCommand.set(null);
    disableListeners();

    if (! stealthy)
      log.info("Project fields have been resetted.");
  }

  /**
   * Loads the selected project and sets the configuration map and the a Path
   * object to the configuration yml.
   *
   * @param projectYml
   * @return True, if everything went well, else false
   */
  private boolean loadProjectConfiguration(Path projectYml) {

    /* Read in the configuration file. */
    HashMap<String, Object> map = null;
    try {
      map = (HashMap<String, Object>) _model.yaml.load(
        new FileInputStream(projectYml.toFile()));
    } catch (IOException e) {
      log.error(e.toString());
    }

    /* Check it for validity. */
    if (! checkConfigForValidity(map)) {
      log.error("Aborted project loading.");
      return false;
    } else {
      _projectConfigs = FXCollections.observableMap(map);
      _configurationYml = projectYml;
      return true;
    }
  }

  /** Saves the project's configuration. */
  private void saveProjectConfiguration() {
    try {
      FileWriter writer = new FileWriter(_configurationYml.toFile());
      _model.yaml.dump(_projectConfigs, writer);
    } catch (IOException ex) {
       log.error("Could not save project configuration file.");
    }
  }

  /**
   * Initializes projects fields.
   *
   * @return True, if everything went well, else false
   */
  private boolean initFields() {

    String abortMessage = "Aborted initializing of project fields.";
    String continueMessage = "Continuing initiliazing of project fields.";

    String filename = _configurationYml.getFileName().toString();
    projectName.set(slice_end(filename, -4));

    rootFolder = _configurationYml.getParent();

    rudiFolder = rootFolder.resolve(PATH_TO_RUDI_FOLDER);
    if (! Files.exists(rudiFolder)) {
      log.error(".rudi folder could not be found. \n"
              + "Should be here: " + rudiFolder.toString() + "\n"
              + abortMessage);
      return false;
    }

    compileFile.set(rootFolder.resolve(COMPILE_FILE));
    if (! Files.exists(compileFile.get())) {
      log.error("Compilation file could not be found. \n"
              + "Should be here: " + compileFile.get() + "\n"
              + abortMessage);
      // No return, as this is not crucial.
    }

    if (_projectConfigs.containsKey("defaultCompileCommand")) {
      _defaultCompileCommand.set((String) _projectConfigs
              .get("defaultCompileCommand"));
    }

    /* Create generated directory (if necessary) */
    Path generatedDirectory = rootFolder.resolve(getGeneratedDirectory());
    if (! Files.exists(generatedDirectory)) {
      generatedDirectory.toFile().mkdirs();
      log.debug("Created " + generatedDirectory);
    }

    /* Create output directory (if necessary) */
    Path outputDirectory = rootFolder.resolve(getOutputDirectory());
    if (! Files.exists(outputDirectory)) {
      outputDirectory.toFile().mkdirs();
      log.debug("Created " + outputDirectory);
    }

    /* Look for RuleLoc.yml */
    ruleLocationFile = rootFolder.resolve(getGeneratedDirectory()
      .resolve(RULE_LOCATION_FILE));
    if (! Files.exists(ruleLocationFile)) {
      log.info("RuleLoc.yml could not be found. \n"
             + "Could be here: " + ruleLocationFile
             + "; but was probably not compiled yet. \n"
             + continueMessage);
    }

    /* Set the RuleModelState save folder */
    _ruleModelStatesFolder = GLOBAL_CONFIG_PATH
      .resolve("loggingConfigurations").resolve(projectName.get());
    if (! Files.exists(_ruleModelStatesFolder))
      _ruleModelStatesFolder.toFile().mkdirs();

    return true;

  }

   /**
   * Checks if a read in map represents a project configuration.
   *
   * @param yml
   * @return true, if all keys could be found, else false
   */
  private boolean checkConfigForValidity(HashMap loadedConfig) {
    boolean b = loadedConfig.keySet()
      .containsAll(DEFAULT_PROJECT_CONFIGURATION_KEYS);
    if (b)
      return true;
    else {
      log.error("Provided project config is invalid.");
      ArrayList<String> missingKeys = new ArrayList<>();
      for (String key: DEFAULT_PROJECT_CONFIGURATION_KEYS) {
        if (! loadedConfig.containsKey(key))
          missingKeys.add(key);
      }
      log.error("Config file is missing the following key(s): " + missingKeys);
      return false;
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
   * GETTERS REPRESENTING CONFIGURATION DETAILS
   ****************************************************************************/

  /** @Return project's configuration .yml file */
  public Path getConfigurationYml() { return _configurationYml; }

  /** @return The property indicating the project's name */
  public StringProperty projectNameProperty() { return projectName; }

  /** @return The project's name */
  public String getProjectName() { return projectName.get(); }

  /** @return The compile file */
  public Path getCompileFile() { return compileFile.get(); }

  /** @return The default compile command */
  public StringProperty defaultCompileCommandProperty() {
    return _defaultCompileCommand;
  }

  /** @return The default compile command */
  public String getDefaultCompileCommand() {
    return _defaultCompileCommand.get();
  }

  /** @return The Path to the RuleLoc.yml */
  public Path getRuleLocationFile() {return ruleLocationFile; }

  /** @return The Path to the RuleModelStates' save folder */
  public Path getRuleModelStatesFolder() { return _ruleModelStatesFolder; }

  /** @return The project's wrapper class */
  public Path getWrapperClass() {
    String longName = (String) _projectConfigs.get("wrapperClass");
    String[] split = longName.split("\\.");
    String shortName = split[split.length-1];
    return rudiFolder.resolve(shortName + RULE_FILE_EXTENSION);
  }

  /** @return The project's output directory (aka gen-java) */
  public Path getOutputDirectory() {
    String c = (String) _projectConfigs.get("outputDirectory");
    return Paths.get(c);
  }

  /**
   * @return the project's generated directory
   * (usually src/main/resources/generated)
   */
  public Path getGeneratedDirectory() {
    return rootFolder.resolve(PATH_TO_GENERATED_FOLDER);
  }

  /** @return The project's root folder */
  public Path getRootFolder() { return rootFolder; }

  /** @return The project's .rudi folder */
  public Path getRudiFolder() { return rudiFolder; }

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
  public LinkedHashMap<String, String> getCustomCompileCommands() {
    if (_projectConfigs.containsKey("customCompileCommands")) {
      return (LinkedHashMap<String, String>) _projectConfigs
        .get("customCompileCommands");
    } else {
      return new LinkedHashMap<>();
    }
  }

}

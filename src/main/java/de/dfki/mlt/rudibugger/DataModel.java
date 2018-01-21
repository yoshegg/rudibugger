/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger;

import de.dfki.lt.j2emacs.J2Emacs;
import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.Controller.SettingsController;
import de.dfki.mlt.rudibugger.FileTreeView.RudiFolderHierarchy;
import de.dfki.mlt.rudibugger.FileTreeView.RudiPath;
import static de.dfki.mlt.rudibugger.Helper.*;
import de.dfki.mlt.rudibugger.RPC.JavaFXLogger;
import de.dfki.mlt.rudibugger.RPC.LogData;
import de.dfki.mlt.rudibugger.RPC.RudibuggerAPI;
import de.dfki.mlt.rudibugger.RPC.RudibuggerClient;
import de.dfki.mlt.rudibugger.RPC.RudibuggerServer;
import de.dfki.mlt.rudibugger.RuleStore.RuleModel;
import de.dfki.mlt.rudibugger.RuleTreeView.RuleTreeViewState;
import de.dfki.mlt.rudibugger.TabManagement.FileAtPos;
import de.dfki.mlt.rudibugger.TabManagement.RudiTab;
import de.dfki.mlt.rudibugger.WatchServices.RudiFolderWatch;
import de.dfki.mlt.rudibugger.WatchServices.RuleLocationWatch;
import static de.dfki.mlt.rudimant.common.Constants.*;
import de.dfki.mlt.rudimant.common.RuleLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * The DataModel represents the business logic of rudibugger.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class DataModel {


  /*****************************************************************************
   * SOME BASIC FIELDS
   ****************************************************************************/

  /** the logger of the DataModel */
  static Logger log = LoggerFactory.getLogger("dataLog");

  /** a YAML instance for further use of YAML */
  public Yaml yaml;

  /** the main stage, necessary when opening additional windows e.g. prompts */
  public Stage stageX;

  /** the global configuration directory */
  public Path globalConfigPath = Paths.get(System.getProperty("user.home"),
          ".config", "rudibugger");


  /*****************************************************************************
   * GLOBAL KNOWLEDGE
   ****************************************************************************/

  public ObservableMap<String, Object> _globalConfigs;
  private Path _globalConfigurationFile;

  public ObservableList<String> _recentProjects;
  private Path _recentProjectsFile;

  /** initialize global knowledge */
  public void initializeGlobalKnowledge() {

    /* get global configuration file */
    ObservableMap<String, Object> globalConfigs;
    _globalConfigurationFile = globalConfigPath.resolve("rudibuggerConfiguration.yml");
    try {
      Map tempMap = (Map<String, String>) yaml.load(
              new FileInputStream(_globalConfigurationFile.toFile()));
      globalConfigs = FXCollections.observableMap(tempMap);
    } catch (FileNotFoundException ex) {
      log.error("No configuration file has been found. Creating a new one...");
      globalConfigs = createGlobalConfigFile();
    }
    _globalConfigs = globalConfigs;


    /* get recent projects */
    ObservableList<String> recentProjects;
    _recentProjectsFile = globalConfigPath.resolve("recentProjects.yml");
    try {
      ArrayList tempList = (ArrayList) yaml.load(
              new FileInputStream(_recentProjectsFile.toFile()));
      recentProjects = FXCollections.observableArrayList(tempList);
    } catch (FileNotFoundException e) {
      log.error("Error while reading in recent projects. "
              + "Maybe the file does not exist (yet)?");
      recentProjects = FXCollections.observableArrayList();
    } catch (NullPointerException e) {
      log.debug("No recent projects could be found");
      recentProjects = FXCollections.observableArrayList();
    }
    _recentProjects = recentProjects;
  }

  /** create global configuration file */
  private ObservableMap<String, Object> createGlobalConfigFile() {
    HashMap tempConfig = new HashMap<String, Object>() {{
      put("editor", "rudibugger");
      put("openFileWith", "");
      put("openRuleWith", "");
      put("timeStampIndex", true);
    }};

    try {
        FileWriter writer = new FileWriter(_globalConfigurationFile.toFile());
        yaml.dump(tempConfig, writer);
      } catch (IOException e) {
        log.error("Could not create global configuration file.");
      }

    return FXCollections.observableMap(tempConfig);
  }

  /** keep global knowledge up-to-date */
  public void keepGlobalKnowledgeUpToDate() {
    /* keep recent projects list updated */
    _recentProjects.addListener((ListChangeListener.Change<? extends String> c) -> {
      yaml.dump(_recentProjects);
      try {
        FileWriter writer = new FileWriter(_recentProjectsFile.toFile());
        yaml.dump(_recentProjects, writer);
      } catch (IOException e) {
        log.error("Could not update recent projects history.");
      }
    });

    /* keep global settings updated */
    _globalConfigs.addListener((MapChangeListener.Change<? extends String, ? extends Object> ml) -> {
      yaml.dump(_globalConfigs);
      try {
        FileWriter writer = new FileWriter(_globalConfigurationFile.toFile());
        yaml.dump(_globalConfigs, writer);
      } catch (IOException e) {
        log.error("Could not update recent projects history.");
      }
    });
  }

  private void addToRecentProjects(Path project) {
    String projPath = project.toString();
    if (_recentProjects.contains(projPath)) {
      _recentProjects.remove(projPath);
    }
    _recentProjects.add(0, projPath);
  }


  /*****************************************************************************
   * THE INITIALIZER METHODS & RESET METHOD
   ****************************************************************************/

  /** initialize the DataModel */
  public void initialize() {
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    yaml = new Yaml(options);
    _compileFile = new SimpleObjectProperty<>(null);
    _runFile = new SimpleObjectProperty<>(null);
    _rudiFolder = new SimpleObjectProperty<>(null);
  }

  /**
   * This method initializes a given project.
   *
   * @param selectedProjectYml the selected .yml file from the file
   * selection dialogue
   * @throws java.io.IOException
   */
  public void initProject(Path selectedProjectYml) throws IOException {
    if (! getConfiguration(selectedProjectYml)) {
      log.error("Given file can not be used to create a project.");
      return;
    }
    log.info("Initializing new project [" + selectedProjectYml.getFileName()
            .toString() + "]");
    addToRecentProjects(selectedProjectYml);
    initProjectFields(selectedProjectYml);
    initProjectWatches();
    readInRudiFiles();
    initRules();
    setProjectStatus(PROJECT_OPEN);
    log.info("Initializing done.");
    connectToRudimant();
  }

  /**
   * This just checks a few things to verify that the given .yml represents a
   * project.
   *
   * @param yml
   * @return true, if all keys could be found, else false
   */
  private boolean getConfiguration(Path yml) {
    HashMap<String, Object> map;
    try {
      map = (HashMap<String, Object>) yaml.load(new FileInputStream(yml.toFile()));
    } catch (IOException e) {
      log.error(e.toString());
      return false;
    }
    HashSet<String> keysToCheckOn = new HashSet() {
      {
        add("outputDirectory");
        add("wrapperClass");
        add("ontologyFile");
        add("rootPackage");
      }
    };
    /* store configuration */
    _configs = map;
    return map.keySet().containsAll(keysToCheckOn);

  }

  private void initProjectWatches() {
    ruleLocWatch = new RuleLocationWatch();
    ruleLocWatch.createRuleLocationWatch(this);
    rudiFolderWatch = new RudiFolderWatch();
    rudiFolderWatch.createRudiFolderWatch(this);
  }

  public void readInRudiFiles() throws IOException {
    Stream<Path> stream = Files.walk(_rudiFolder.getValue());
    stream.forEach(x -> {
      if (x.getFileName().toString().endsWith(RULE_FILE_EXTENSION)
              || Files.isDirectory(x))
        rudiHierarchy.addFileToHierarchy(new RudiPath(x.toAbsolutePath()));
    });
  }

  public void initRules() {
    _ruleLocFile = _rootFolder.resolve(_generatedFolder.resolve(RULE_LOCATION_FILE));
    if (Files.exists(_ruleLocFile)) {
      log.debug(_ruleLocFile.getFileName().toString() + " has been found.");
      ruleModel = RuleModel.createNewRuleModel();
      ruleModel.readInRuleModel(_ruleLocFile, _rudiFolder.getValue());
      setRuleModelChangeStatus(RULE_MODEL_NEWLY_CREATED);
    } else {
      _ruleLocFile = null;
      log.warn(_projectName.getValue() + "'s " + RULE_LOCATION_FILE
              + " could not be found.");
    }
  }

  private void initProjectFields(Path selectedProjectYml) {
    _projectName = new SimpleStringProperty(slice_end(selectedProjectYml
            .getFileName().toString(), -4));
    _rootFolder = selectedProjectYml.getParent();

    Path rudiFolder = (Paths.get(_rootFolder + "/" + PATH_TO_RUDI_FILES));
    if (Files.exists(rudiFolder)) {
      log.debug(".rudi folder has been found.");
      _rudiFolder.setValue(rudiFolder);
    } else {
      log.error(".rudi folder could not be found.");
      _rudiFolder.setValue(null);
    }

    rudiHierarchy = new RudiFolderHierarchy(_rudiFolder.getValue());

    Path compilePath = Paths.get(_rootFolder.toString() + "/"  + COMPILE_FILE);
    if (Files.exists(compilePath)) {
      _compileFile.setValue(compilePath);
      log.debug(_compileFile.getValue().getFileName().toString()
              + " has been found.");
    } else {
      log.info(_projectName.getValue() + "'s " + COMPILE_FILE
              + " could not be found.");
    }

    Path runPath = Paths.get(_rootFolder.toString() + "/"  + RUN_FILE);
    if (Files.exists(runPath)) {
      _runFile.setValue(runPath);
      log.debug(_runFile.getValue().getFileName().toString()
              + " has been found.");
    } else {
      log.info(_projectName.getValue() + "'s " + RUN_FILE
              + " could not be found.");
    }

    /* set the generated/ folder path */
    _generatedFolder = _rootFolder.resolve(Paths.get("src/main/resources/generated"));
    if (! Files.exists(_generatedFolder)) {
      _generatedFolder.toFile().mkdirs();
      log.debug("Created " + _generatedFolder);
    }

    /* set the wrapper class */
    String temp = (String) _configs.get("wrapperClass");
    String[] split = temp.split("\\.");
    String wrapperName = split[split.length-1];
    _wrapperClass = _rudiFolder.getValue().resolve(wrapperName + RULE_FILE_EXTENSION);

  }

  public void closeProject() {
    log.info("Closing [" + _projectName.getValue() + "]...");
    _compileFile.setValue(null);
    _runFile.setValue(null);
    _rudiFolder.setValue(null);
    ruleModel = null;
    ruleLocWatch.shutDownListener();
    rudiFolderWatch.shutDownListener();
    closeConnectionToRudimant();
    setRuleModelChangeStatus(RULE_MODEL_REMOVED);
    setProjectStatus(PROJECT_CLOSED);
  }

  /*****************************************************************************
   * UPDATE METHODS FOR THE CURRENT PROJECT AKA DATAMODEL
   ****************************************************************************/

  public void updateProject() {
    updateRules();
  }

  private void updateRules() {
    log.debug("Updating the RuleModel");
    ruleModel.updateRuleModel(_ruleLocFile, _rudiFolder.getValue());
    setRuleModelChangeStatus(RULE_MODEL_CHANGED);
  }

  public void removeRudiPath(Path file) {
    rudiHierarchy.removeFromFileHierarchy(new RudiPath(file));
  }

  /**
   * This function adds a newly appeared file to the rudiTreeView. Unfortunately
   * it may be that this file has been modified externally and therefore is
   * recognized as ENTRY_CREATE first. Because of this true or false are
   * returned so that the logger may log correctly.
   *
   * @param file
   * @return true if added, false if not added (already there)
   */
  public boolean addRudiPath(Path file) {
    return rudiHierarchy.addFileToHierarchy(new RudiPath(file));
  }


  /*****************************************************************************
   * FILE MANAGEMENT (OPENING, SAVING etc.)
   ****************************************************************************/

  /**
   * This function needs to be called when a given file should be opened.
   * Depending on a specific setting, it will be opened in a rudibugger tab or
   * in an external application.
   *
   * @param file the wanted file
   */
  public void openFile(Path file) {
    switch ((String) _globalConfigs.get("editor")) {
      case "rudibugger":
        requestTabOfFile(file);
        return;
      case "emacs":
        if (! isEmacsAlive()) {
          startEmacsConnection("emacs");
        }
        _j2e.visitFilePosition(file.toFile(), 1, 0, "");
        return;
      case "custom":
        try {
          String cmd = ((String) _globalConfigs.get("openFileWith"))
                  .replaceAll("%file", file.toString());
          Runtime.getRuntime().exec(cmd);
          return;
        } catch (IOException ex) {
          log.error("Can't use custom editor to open file. ");
          break;
        }
      default:
        break;
    }
    log.info("No valid file editor setting has been found. Using rudibugger.");
        requestTabOfFile(file);
  }

  /**
   * This function needs to be called when a given rule should be opened.
   * Depending on a specific setting, it will be opened in a rudibugger tab or
   * in an external application
   *
   * @param file the wanted file
   * @param position the line of the wanted rule
   */
  public void openRule(Path file, Integer position) {
    switch ((String) _globalConfigs.get("editor")) {
      case "rudibugger":
        requestTabOfRule(file, position);
        return;
      case "emacs":
        if (! isEmacsAlive()) {
          startEmacsConnection("emacs");
        }
        _j2e.visitFilePosition(file.toFile(), position, 0, "");
        return;
      case "custom":
        try {
          String cmd = ((String) _globalConfigs.get("openRuleWith"))
                  .replaceAll("%file", file.toString())
                  .replaceAll("%line", position.toString());
          Runtime.getRuntime().exec(cmd);
          return;
        } catch (IOException ex) {
          log.error("Can't use custom editor to open file. ");
          break;
        }
      default:
        break;
    }
    log.info("No valid file editor setting has been found. Using rudibugger.");
    requestTabOfRule(file, position);
  }


  /**
   * This function needs to be called when a new tab showing a certain file
   * should be opened.
   *
   * @param file the wanted file
   */
  private void requestTabOfFile(Path file) {
    requestTabOfRule(file, 1);
  }

  /**
   * This function needs to be called when a new tab showing a certain rule from
   * a specific file should be opened.
   *
   * @param file the wanted file
   * @param position the line of the wanted rule
   */
  private void requestTabOfRule(Path file, Integer position) {
    FileAtPos temp = new FileAtPos(file, position);
    requestedFile.setValue(temp);
  }

  /**
   * This function is called when a file should be <b>quick-saved</b> (overwrite
   * the old version of the file).
   */
  public void updateFile() {
    RudiTab tab = selectedTab.getValue();
    Path file = tab.getFile();
    String content = tab.getRudiCode();

    if (saveFile(file, content)) {
      tab.setText(file.getFileName().toString());
      tab.waitForModif();
      log.debug("File " + file.getFileName() + " has been saved.");
      notifySaved(file.getFileName().toString());
    }
  }

  /**
   * This function is called when the content of a tab should be saved as a new
   * file.
   */
  public void saveFileAs() {
    RudiTab tab = selectedTab.getValue();
    String content = tab.getRudiCode();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(_rudiFolder.getValue().toFile());
    FileChooser.ExtensionFilter extFilter
            = new FileChooser.ExtensionFilter
        ("rudi file (*" + RULE_FILE_EXTENSION + ")", "*" + RULE_FILE_EXTENSION);
    fileChooser.getExtensionFilters().add(extFilter);
    Path file;
    try {
      file = (fileChooser.showSaveDialog(stageX)).toPath();
    } catch (NullPointerException e) {
      return;
    }
      if (! file.getFileName().toString().endsWith(RULE_FILE_EXTENSION)) {
        file = Paths.get(file.toString() + RULE_FILE_EXTENSION);
    }

    if (saveFile(file, content)) {

      /* close old tab */
      EventHandler<Event> handler = tab.getOnClosed();
      if (null != handler) {
        handler.handle(null);
      } else {
        requestedCloseTabProperty().setValue(tab);
        tab.getTabPane().getTabs().remove(tab);
      }

      /* open a new tab */
      openFile(file);

      log.debug("File " + file.getFileName() + " has been saved.");
    }
  }

  /**
   * This hidden function is called by every save request.
   *
   * @param file the path of the to-be-saved file
   * @param content the content of the to-be-saved file
   * @return
   */
  private boolean saveFile(Path file, String content) {
    try {
      Files.write(file, content.getBytes());
      return true;
    } catch (IOException e) {
      log.error("could not save " + file);
      return false;
    }
  }

  /******** Properties **********/

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
  private void setStatusBar(String value) { statusBarProperty().set(value); }
  public String getStatusBar() { return statusBarProperty().get(); }
  public StringProperty statusBarProperty() { return statusBar; }

  /**
   * This function changes the statusBar temporarily to indicate that a file has
   * been saved.
   *
   * @param file the file that has been saved
   */
  private void notifySaved(String file) {
    setStatusBar("Saved " + file + ".");
    PauseTransition pause = new PauseTransition(Duration.seconds(3));
    pause.setOnFinished(e -> setStatusBar(null));
    pause.play();
  }

  /*****************************************************************************
   * UNDERLYING FIELDS / PROPERTIES OF THE CURRENT PROJECT AKA DATAMODEL
   ****************************************************************************/

  /** Configuration as defined in the project's .yml */
  private Map<String, Object> _configs;

  /** the RuleModel represents all the data concerning rules */
  public RuleModel ruleModel;

  /** this variable is set to true if the ruleModel has been changed */
  private final IntegerProperty ruleModelChanged
          = new SimpleIntegerProperty(RULE_MODEL_UNCHANGED);
  public void setRuleModelChangeStatus(int val) { ruleModelChanged.set(val); }
  public IntegerProperty ruleModelChangeProperty() { return ruleModelChanged; }

  /** RootFolder (where the project sleeps) */
  private Path _rootFolder;
  public Path getRootFolder() { return _rootFolder; }

  /** RudiFolder (where .rudi files sleep) */
  private ObjectProperty<Path> _rudiFolder;
  public Path getRudiFolder() { return _rudiFolder.getValue(); }
  public ObjectProperty<Path> rudiFolderProperty() { return _rudiFolder; }

  /** .rudi files */
  public TreeItem<RudiPath> rudiList;
  public RudiFolderHierarchy rudiHierarchy;

  /** output aka gen-java location */
  public Path _generatedFolder;

  /** RuleLocationFile */
  private Path _ruleLocFile;
  public Path getRuleLocFile() { return _ruleLocFile; }

  /** The project's name */
  private StringProperty _projectName;
  public String getProjectName() { return _projectName.getValue(); }
  public StringProperty projectNameProperty() { return _projectName; }

  /** CompileFile */
  private ObjectProperty<Path> _compileFile;
  public Path getCompileFile() { return _compileFile.getValue(); }
  public ObjectProperty<Path> compileFileProperty() { return _compileFile; }

  /** RunFile */
  private ObjectProperty<Path> _runFile;
  public Path getRunFile() { return _runFile.getValue(); }
  public ObjectProperty<Path> runFileProperty() { return _runFile; }

  /** WrapperClass */
  private Path _wrapperClass;
  public Path getWrapperClass() { return _wrapperClass; }

  /** Project status */
  private final IntegerProperty _projectStatus
          = new SimpleIntegerProperty(PROJECT_CLOSED);
  public void setProjectStatus(int val) { _projectStatus.set(val); }
  public IntegerProperty projectStatusProperty() { return _projectStatus; }

  /* The WatchServices */
  private RuleLocationWatch ruleLocWatch;
  private RudiFolderWatch rudiFolderWatch;


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  public void startCompile() throws IOException, InterruptedException {
    log.info("Starting compilation...");
    File mateTerminal = new File("/usr/bin/mate-terminal");
    Process p;
    String compileScript = getCompileFile().toString();
    if ("Linux".equals(System.getProperty("os.name"))) {
      String[] cmd;
      if (mateTerminal.exists()) {
        cmd = new String[] {"/usr/bin/mate-terminal", "-e", "sh -c 'cd "
          + getRootFolder().toString() + ";" + compileScript + "'"};
      } else {
        cmd = new String[] { "/usr/bin/xterm", "-e", "sh -c 'cd "
          + getRootFolder().toString() + ";" + compileScript + "'"};
      }
      log.debug("Executing the following command: " + Arrays.toString(cmd));

      p = Runtime.getRuntime().exec(cmd);
    } else {
      p = Runtime.getRuntime().exec(getCompileFile().toString());
    }
  }

  public void createNewProject() {
    log.info("Not implemented yet.");
  }

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


  /*****************************************************************************
   * RULE LOGGING STATE MODEL
   ****************************************************************************/


  /* SAVE SELECTION */

  /** Used to signalize the save request of a ruleLoggingState */
  private final SimpleBooleanProperty _ruleLoggingStateSaveRequestProperty
    = new SimpleBooleanProperty(false);

  /**
   * Used in a Controller to listen to property.
   *
   * @return
   */
  public BooleanProperty ruleLoggingStateSaveRequestProperty() {
    return _ruleLoggingStateSaveRequestProperty;
  }

  public void resetRuleLoggingStateSaveRequestProperty() {
    _ruleLoggingStateSaveRequestProperty.setValue(Boolean.FALSE);
  }

  /** Request to save the current ruleLoggingState selection */
  public void requestSaveRuleLoggingState() {
    _ruleLoggingStateSaveRequestProperty.setValue(Boolean.TRUE);
  }

  /**
   * Save the current ruleLoggingState selection
   *
   * @param rtvs
   */
  public void saveRuleLoggingState(RuleTreeViewState rtvs) {
    Path savePath = globalConfigPath.resolve(_projectName.get());
    if (! Files.exists(savePath)) savePath.toFile().mkdirs();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(savePath.toFile());
    FileChooser.ExtensionFilter extFilter
            = new FileChooser.ExtensionFilter
        ("YAML file (*" + "yml" + ")", "*" + "yml");
    fileChooser.getExtensionFilters().add(extFilter);
    Path file;
    try {
      file = (fileChooser.showSaveDialog(stageX)).toPath();
    } catch (NullPointerException e) {
      return;
    }

    if (!file.getFileName().toString().endsWith(".yml")) {
      file = Paths.get(file.toString() + ".yml");
    }

    try {
      FileWriter writer = new FileWriter(savePath.resolve(file).toFile());
      yaml.dump(rtvs, writer);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
    log.debug("Saved file " + file.toString());

  }


  /* LOAD SELECTION */

  /** used to signalize the load request of a ruleLoggingState */
  private final SimpleObjectProperty<Path> _ruleLoggingStateLoadRequestProperty
    = new SimpleObjectProperty<>();

  /**
   * Used in a Controller to listen to property.
   *
   * @return
   */
  public ObjectProperty ruleLoggingStateLoadRequestProperty() {
    return _ruleLoggingStateLoadRequestProperty;
  }

  /**
   * load a given file as ruleSelectionState
   *
   * @param x
   */
  public void loadRuleLoggingState(Path x) {
    _ruleLoggingStateLoadRequestProperty.setValue(x);
  }

  /* RULE LOGGING STATE */

  private SimpleObjectProperty<RuleTreeViewState> _ruleLoggingStateProperty
    = new SimpleObjectProperty<>();

  public ObjectProperty ruleLoggingStateProperty() {
    return _ruleLoggingStateProperty;
  }

  private ArrayList<Path> _ruleLoggingStates;
  public ArrayList<Path> getRuleLoggingStates() { return _ruleLoggingStates; }

  private void readInRuleLoggingStates() {};



  /*****************************************************************************
   * CONNECTION TO EMACS
   ****************************************************************************/

  /** An Emacs connector */
  private J2Emacs _j2e = null;

  public void startEmacsConnection(String emacsPath) {

    File emacsLispPath = new File("src/main/resources/emacs/");
    _j2e = new J2Emacs("Rudibugger", emacsLispPath, null);
    _j2e.addStartHook(
        "(setq auto-mode-alist (append (list '(\"\\\\.rudi\" . java-mode))))");
    _j2e.startEmacs();
  }

  public boolean isEmacsAlive() {
    return _j2e != null && _j2e.alive();
  }

  public void closeEmacs(boolean quitEmacs) {
    if (_j2e == null) return;
    if (quitEmacs) {
      _j2e.exitEmacs();
    }
    _j2e.close();
    _j2e = null;
  }


  /*****************************************************************************
   * CONNECTION TO RUDIMANT
   ****************************************************************************/

  public RudibuggerServer rs;
  public RudibuggerClient vonda;

  private void connectToRudimant() throws IOException {
    int rudibuggerPort = ((_configs.get("SERVER_RUDIBUGGER") == null)
            ? SERVER_PORT_RUDIBUGGER : (int) _configs.get("SERVER_RUDIBUGGER"));
    int rudimantPort = ((_configs.get("SERVER_RUDIMANT") == null)
            ? SERVER_PORT_RUDIMANT : (int) _configs.get("SERVER_RUDIMANT"));

    rs = new RudibuggerServer(new RudibuggerAPI(this));
    rs.startServer(rudibuggerPort);
    log.debug("RudibuggerServer has been started "
            + "on port [" + rudibuggerPort + "].");
    vonda = new RudibuggerClient(rudimantPort);
    log.debug("RudibuggerClient has been started and is looking for rudimant "
            + "(server) on port [" + rudimantPort + "].");
  }

  private void closeConnectionToRudimant() {
    try {
      vonda.disconnect();
    } catch (IOException e) {
      log.error(e.toString());
    }
    rs.stopServer();
  }

  private RuleLogger rl;
  private JavaFXLogger jfl;

  private void initializeRuleLogger() {
    rl = new RuleLogger();
    rl.setRootInfo(ruleModel.rootImport);
    jfl = new JavaFXLogger();
    rl.setPrinter(jfl);
    rl.logAllRules();
  }

  public void printLog(int ruleId, boolean[] result) {
    /* Lazy initializing */
    if (rl == null) {
      initializeRuleLogger();
    }

    Platform.runLater(() -> {
      if (!getConnectedToRudimant())
        connectedToRudimantProperty().setValue(true);
      rl.logRule(ruleId, result);
      if (jfl.pendingLoggingData()) {
        jfl.addRuleIdToLogData(ruleId);
        rudiLogOutput.setValue(jfl.popContent());
      }
    });

  }

  /******** Properties **********/

  private final BooleanProperty connectedToRudimant
          = new SimpleBooleanProperty(false);
  public boolean getConnectedToRudimant() {
    return connectedToRudimant.getValue();
  }
  public BooleanProperty connectedToRudimantProperty() {
    return connectedToRudimant;
  }

  private final ObjectProperty<LogData> rudiLogOutput
    = new SimpleObjectProperty<>();
  public ObjectProperty<LogData> rudiLogOutputProperty() {
    return rudiLogOutput;
  }

}

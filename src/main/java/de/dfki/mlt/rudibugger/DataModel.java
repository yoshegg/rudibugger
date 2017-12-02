/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger;

import static de.dfki.mlt.rudibugger.Constants.*;
import static de.dfki.mlt.rudimant.common.Constants.*;
import static de.dfki.mlt.rudibugger.Helper.*;
import de.dfki.mlt.rudibugger.FileTreeView.RudiFolderHierarchy;
import de.dfki.mlt.rudibugger.FileTreeView.RudiPath;
import de.dfki.mlt.rudibugger.RPC.RudibuggerAPI;
import de.dfki.mlt.rudibugger.RPC.RudibuggerClient;
import de.dfki.mlt.rudibugger.RPC.RudibuggerServer;
import de.dfki.mlt.rudibugger.RuleStore.RuleModel;
import de.dfki.mlt.rudibugger.TabManagement.FileAtPos;
import de.dfki.mlt.rudibugger.TabManagement.RudiTab;
import de.dfki.mlt.rudibugger.WatchServices.RudiFolderWatch;
import de.dfki.mlt.rudibugger.WatchServices.RuleLocationWatch;
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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
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
  public Path globalConfig = Paths.get(System.getProperty("user.home"),
          ".config", "rudibugger");


  /*****************************************************************************
   * GLOBAL KNOWLEDGE
   ****************************************************************************/

  public ObservableList<String> _recentProjects;
  private Path _recentProjectsFile;

  /** initialize global knowledge */
  public void initializeGlobalKnowledge() {

    /* get recent projects */
    ObservableList<String> recentProjects;
    _recentProjectsFile = globalConfig.resolve("recentProjects.yml");
    try {
      ArrayList tempList = (ArrayList) yaml.load(
              new FileInputStream(_recentProjectsFile.toFile()));
      recentProjects = FXCollections.observableArrayList(tempList);
    } catch (FileNotFoundException e) {
      log.error("Error while reading in recent projects");
      recentProjects = FXCollections.observableArrayList();
    } catch (NullPointerException e) {
      log.debug("No recent projects could be found");
      recentProjects = FXCollections.observableArrayList();
    }
    _recentProjects = recentProjects;
  }

  /** keep global knowledge up-to-date */
  public void keepGlobalKnowledgeUpToDate() {
    _recentProjects.addListener((ListChangeListener.Change<? extends String> c) -> {
      yaml.dump(_recentProjects);
      try {
        FileWriter writer = new FileWriter(_recentProjectsFile.toFile());
        yaml.dump(_recentProjects, writer);
      } catch (IOException e) {
        log.error("could not update recent projects history.");
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
    ruleModel.readInRuleModel(_ruleLocFile, _rudiFolder.getValue());
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
   * This function needs to be called when a new tab showing a certain file
   * should be opened.
   *
   * @param file the wanted file
   */
  public void requestTabOfFile(Path file) {
    requestTabOfRule(file, 1);
  }

  /**
   * This function needs to be called when a new tab showing a certain rule from
   * a specific file should be opened.
   *
   * @param file the wanted file
   * @param position the line of the wanted rule
   */
  public void requestTabOfRule(Path file, Integer position) {
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
      requestTabOfFile(file);

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
    log.debug("RudibuggerClient has been started and looks for rudimant "
            + "(server) on port [" + rudimantPort + "].");
  }

  private void closeConnectionToRudimant() throws IOException {
    vonda.disconnect();
  }
}

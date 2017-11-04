/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger;

import static de.dfki.rudibugger.Constants.*;
import de.dfki.rudibugger.RuleStore.RuleModel;
import de.dfki.rudibugger.WatchServices.RuleLocationWatch;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import static de.dfki.rudibugger.Helper.*;
import de.dfki.rudibugger.RudiList.RudiPath;
import de.dfki.rudibugger.TabManagement.FileAtPos;
import de.dfki.rudibugger.TabManagement.RudiTab;
import de.dfki.rudibugger.WatchServices.RudiFolderWatch;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;

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
  static Logger log = Logger.getLogger("dataLog");

  /** a YAML instance for further use of YAML */
  public Yaml yaml;

  /** the main stage, necessary when opening additional windows e.g. prompts */
  public Stage stageX;


  /*****************************************************************************
   * THE INITIALIZER METHODS & RESET METHOD
   ****************************************************************************/

  /** initialize the DataModel */
  public void initialize() {
    yaml = new Yaml();
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
    log.info("Initializing new project [" + selectedProjectYml.getFileName()
            .toString() + "]");
    initProjectFields(selectedProjectYml);
    initProjectWatches();
    readInRudiFiles();
    initRules();
    setProjectStatus(PROJECT_OPEN);
    log.info("Initializing done.");
  }

  private void initProjectWatches() {
    ruleLocWatch = new RuleLocationWatch();
    ruleLocWatch.createRuleLocationWatch(this);
    rudiFolderWatch = new RudiFolderWatch();
    rudiFolderWatch.createRudiFolderWatch(this);
  }

  private void readInRudiFiles() throws IOException {
    rudiList = FXCollections.observableArrayList();

    /* get a sorted list of this directory's files */
    DirectoryStream<Path> stream = Files.newDirectoryStream(
            _rudiFolder.getValue());
    List<Path> list = new ArrayList<>();
    stream.forEach(list::add);
    list.sort(
      (Path h1, Path h2) -> h1.getFileName().toString().toLowerCase()
              .compareTo(h2.getFileName().toString().toLowerCase()));

    list.forEach((x) -> {
      if (x.getFileName().toString().endsWith(".rudi")) {
        rudiList.add(new RudiPath(x));
      }
    });
  }


  public void initRules() {
    _ruleLocFile = Paths.get(_rootFolder.toString() + "/"
            + _projectName.getValue() + RULE_LOCATION_SUFFIX);
    if (Files.exists(_ruleLocFile)) {
      log.debug(_ruleLocFile.getFileName().toString() + " has been found.");
      ruleModel = RuleModel.createNewRuleModel();
      ruleModel.readInRuleLocationFileFirstTime(_ruleLocFile);
      setRuleModelChangeStatus(RULE_MODEL_NEWLY_CREATED);
    } else {
      _ruleLocFile = null;
      log.warn(_projectName.getValue() + "'s " + RULE_LOCATION_SUFFIX
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
    ruleModel.updateRuleModel(_ruleLocFile);
    setRuleModelChangeStatus(RULE_MODEL_CHANGED);
  }

  public void removeRudiPath(Path file) {
    rudiList.remove(new RudiPath(file));
  }

  /**
   * This function adds a newly appeared file to the rudiList. Unfortunately it
   * may be that this file has been modified externally and therefore is
   * recognized as ENTRY_CREATE first. Because of this true or false are
   * returned so that the logger may log correctly.
   *
   * @param file
   * @return true if added, false if not added (already there)
   */
  public boolean addRudiPath(Path file) {
    RudiPath temp = new RudiPath(file);
    if (! rudiList.contains(temp)) {
      rudiList.add(new RudiPath(file));
      rudiList.sort((a, b) -> a.getPath().toString().toLowerCase()
              .compareTo(b.getPath().toString().toLowerCase()));
      return true;
    } else {
      return false;
    }
  }

  /*****************************************************************************
   * FILE MANAGEMENT (OPENING, SAVING etc.)
   ****************************************************************************/

  private final ObjectProperty<FileAtPos> requestedFile
          = new SimpleObjectProperty<>();

  public ObjectProperty<FileAtPos> requestedFileProperty() {
    return requestedFile;
  }

  public void requestTabOfFile(Path file) {
    requestTabOfRule(file, 1);
  }

  public void requestTabOfRule(Path file, Integer position) {
    FileAtPos temp = new FileAtPos(file, position);
    requestedFile.setValue(temp);
  }

  public void updateFile() {
    RudiTab tab = selectedTab.getValue();
    Path file = tab.getFile();
    String content = tab.getRudiCode();

    if (saveFile(file, content)) {
      tab.setText(file.getFileName().toString());
      tab.waitForModif();
      log.debug("File " + file.getFileName() + " has been saved.");
    }
  }

  public void saveFileAs() {
    RudiTab tab = selectedTab.getValue();
    String content = tab.getRudiCode();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(_rudiFolder.getValue().toFile());
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("rudi file (*.rudi)", "*.rudi");
    fileChooser.getExtensionFilters().add(extFilter);
    Path file = (fileChooser.showSaveDialog(stageX)).toPath();
    if (! file.getFileName().toString().endsWith(".rudi")) {
      file = Paths.get(file.toString() + ".rudi");
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

  private boolean saveFile(Path file, String content) {
    try {
      Files.write(file, content.getBytes());
      return true;
    } catch (IOException e) {
      log.error("could not save " + file);
      return false;
    }
  }

  private final ObjectProperty<RudiTab> selectedTab
          = new SimpleObjectProperty<>();
  public ObjectProperty<RudiTab> selectedTabProperty() { return selectedTab; }

  private final ObjectProperty<RudiTab> requestedCloseTab
          = new SimpleObjectProperty<>();
  public ObjectProperty<RudiTab> requestedCloseTabProperty() {
    return requestedCloseTab;
  }


  /*****************************************************************************
   * UNDERLYING FIELDS / PROPERTIES OF THE CURRENT PROJECT AKA DATAMODEL
   ****************************************************************************/

  /** the RuleModel represents all the data concerning rules */
  public RuleModel ruleModel;

  /** this variable is set to true if the ruleModel has been changed */
  private final IntegerProperty ruleModelChanged
          = new SimpleIntegerProperty(RULE_MODEL_UNCHANGED);
  public void setRuleModelChangeStatus(int val) { ruleModelChanged.set(val); }
  public IntegerProperty ruleModelChangeProperty() { return ruleModelChanged; }

  /** statusBar */
  private StringProperty statusBar;
  public void setStatusBar(String value) { statusBarProperty().set(value); }
  public String getStatusBar() { return statusBarProperty().get(); }
  public StringProperty statusBarProperty() {
      if (statusBar == null) statusBar = new SimpleStringProperty(this, "firstName"); // TODO
      return statusBar;
  }

  /** RootFolder (where the project sleeps) */
  private Path _rootFolder;
  public Path getRootFolder() { return _rootFolder; }

  /** RudiFolder (where .rudi files sleep) */
  private ObjectProperty<Path> _rudiFolder;
  public Path getRudiFolder() { return _rudiFolder.getValue(); }
  public ObjectProperty<Path> rudiFolderProperty() { return _rudiFolder; }

  /** .rudi files */
  public ObservableList<RudiPath> rudiList;

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
    Process p;
    String compileScript = getCompileFile().toString();
    String[] cmd = { "/usr/bin/xterm", "-e", compileScript, "-b"};
    log.debug("Executing the following command: " + Arrays.toString(cmd));
    if ("Linux".equals(System.getProperty("os.name"))) {
      p = Runtime.getRuntime().exec(cmd);
    } else {
      p = Runtime.getRuntime().exec(getCompileFile().toString() + "-b");
    }
  }

  public void createNewProject() {
    log.info("Not implemented yet.");
  }

}

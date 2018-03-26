/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.Controller.SettingsController;
import de.dfki.mlt.rudibugger.DataModelAdditions.*;
import de.dfki.mlt.rudibugger.FileTreeView.*;
import static de.dfki.mlt.rudibugger.Helper.*;
import de.dfki.mlt.rudibugger.RuleTreeView.*;
import de.dfki.mlt.rudibugger.TabManagement.*;
import de.dfki.mlt.rudibugger.WatchServices.*;
import static de.dfki.mlt.rudimant.common.Constants.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

  public DataModel() {}


  /*****************************************************************************
   * SOME BASIC FIELDS
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

  /** Provides additional functionality about project specific information. */
  public ProjectManager project = new ProjectManager(this);

  /** Provides additional functionality concerning global configuration. */
  public GlobalConfiguration globalConf = new GlobalConfiguration(this);

  /** Provides additional functionality to start VOnDAs compiler. */
  public VondaCompilation compiler = new VondaCompilation(this);


  /*****************************************************************************
   * THE PROJECT INITIALIZER AND CLOSE METHODS
   ****************************************************************************/

  /**
   *
   * @param selectedProjectYml
   */
  public void init(Path selectedProjectYml) {

    project.initConfiguration(selectedProjectYml);



    initProjectFields();
    initProjectWatches();
    readInRudiFiles();

    if (Files.exists(project.getRuleLocationFile()))
      initRules();

    setProjectStatus(PROJECT_OPEN);

    globalConf.addToRecentProjects(selectedProjectYml);
    globalConf.setSetting("lastOpenedProject",
                       selectedProjectYml.toAbsolutePath().toString());
    log.info("Initializing done.");

    /* Link to VOnDA's server. */
    vonda.connect();
  }

  /**
   * Closes a project by nullifying the fields.
   *
   * @param stealthy
   */
   public void close(boolean stealthy) {
    log.info("Closing [" + project.getProjectName() + "]...");

    project.resetConfiguration(false);

    ruleModel = null;
    ruleLocWatch.shutDownListener();
    rudiFolderWatch.shutDownListener();
    vonda.closeConnection();
    setRuleModelChangeStatus(RULE_MODEL_REMOVED);
    setProjectStatus(PROJECT_CLOSED);
    globalConf.setSetting("lastOpenedProject", "");
  }



  /*****************************************************************************
   * OLD
   ****************************************************************************/




  private void initProjectWatches() {
    ruleLocWatch = new RuleLocationWatch();
    ruleLocWatch.createRuleLocationWatch(this);
    rudiFolderWatch = new RudiFolderWatch();
    rudiFolderWatch.createRudiFolderWatch(this);
  }

  public void readInRudiFiles() {
    Stream<Path> stream;
    try {
      stream = Files.walk(project.getRudiFolder());
    } catch (IOException e) {
      log.error(e.toString());
      return;
    }
    stream.forEach(x -> {
      if (x.getFileName().toString().endsWith(RULE_FILE_EXTENSION)
              || Files.isDirectory(x))
        rudiHierarchy.addFileToHierarchy(new RudiPath(x.toAbsolutePath()));
    });
  }

  public void initRules() {
      ruleModel = RuleModel.createNewRuleModel(this);
      ruleModel.readInRuleModel(project.getRuleLocationFile(),
                                project.getRudiFolder());
      setRuleModelChangeStatus(RULE_MODEL_NEWLY_CREATED);
  }

  private void initProjectFields() {

    rudiHierarchy = new RudiFolderHierarchy(project.getRudiFolder());

    /* set the ruleLoggingStates folder */
    _ruleLoggingStatesFolder = GLOBAL_CONFIG_PATH
      .resolve("loggingConfigurations")
      .resolve(project.projectNameProperty().get());

  }

  /*****************************************************************************
   * UPDATE METHODS FOR THE CURRENT PROJECT AKA DATAMODEL
   ****************************************************************************/

  public void updateProject() {
    updateRules();
  }

  private void updateRules() {
    log.debug("Updating the RuleModel");
    ruleModel.updateRuleModel(project.getRuleLocationFile(),
                              project.getRudiFolder());
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

  public void setFileHasBeenModified(Path file) {
    rudiHierarchy.rudiPathMap.get(file)._modifiedProperty().setValue(true);
    _modifiedFiles.setValue(FILES_OUT_OF_SYNC);
  }

  public void setFilesUpToDate() {
    rudiHierarchy.resetModifiedProperties();
    _modifiedFiles.setValue(FILES_SYNCED);
  }

  private final IntegerProperty _modifiedFiles
          = new SimpleIntegerProperty(FILES_SYNC_UNDEFINED);
  public IntegerProperty _modifiedFilesProperty() {
    return _modifiedFiles;
  }

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

  /** the RuleModel represents all the data concerning rules */
  public RuleModel ruleModel;

  /** this variable is set to true if the ruleModel has been changed */
  private final IntegerProperty ruleModelChanged
          = new SimpleIntegerProperty(RULE_MODEL_UNCHANGED);
  public void setRuleModelChangeStatus(int val) { ruleModelChanged.set(val); }
  public IntegerProperty ruleModelChangeProperty() { return ruleModelChanged; }

  /** .rudi files */
  public TreeItem<RudiPath> rudiList;
  public RudiFolderHierarchy rudiHierarchy;

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

  /** project's specific configuration file */
  private Path _ruleLoggingStatesFolder;

  /** project's folder for ruleLoggingState configurations */

  /** list of recent ruleLoggingStates */
  private ArrayList<Path> _recentRuleLoggingStates;



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
    Path savePath = _ruleLoggingStatesFolder;
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
  public ObjectProperty<Path> ruleLoggingStateLoadRequestProperty() {
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

  public void openRuleLoggingStateFileChooser() {
    loadRuleLoggingState(HelperWindows.openRuleLoggingStateFile(
      stageX, _ruleLoggingStatesFolder));
  }

  /* RULE LOGGING STATE */

  private final SimpleObjectProperty<RuleTreeViewState> _ruleLoggingStateProperty
    = new SimpleObjectProperty<>();

  public ObjectProperty ruleLoggingStateProperty() {
    return _ruleLoggingStateProperty;
  }

  public ArrayList<Path> getRecentRuleLoggingStates() {
    return _recentRuleLoggingStates;
  }

}

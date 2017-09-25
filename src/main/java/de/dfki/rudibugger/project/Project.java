/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.project;

import static de.dfki.rudibugger.Constants.*;
import static de.dfki.rudibugger.Helper.*;
import de.dfki.rudibugger.WatchServices.Watch;
import de.dfki.rudibugger.ruleTreeView.BasicTreeItem;
import de.dfki.rudibugger.ruleTreeView.ImportTreeItem;
import de.dfki.rudibugger.ruleTreeView.RuleTreeItem;
import de.dfki.rudibugger.tabs.RudiHBox;
import de.dfki.rudibugger.tabs.RudiTabPane;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javafx.scene.control.TreeView;
import org.apache.log4j.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;

/**
 * An instance of this class contains all the relevant information about
 * the project
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Project {

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /* diverse private fields */
  private Path _compileFile;
  private Path _runFile;
  private Path _ymlFile;
  private Path _rootFolder;
  private Path _rudisFolder;
  private Path _ruleLocFile;
  private String _projName;

  /* diverse GUI elements */
  private TreeView _ruleTreeView;
  private TreeView _fileTreeView;
  private RudiHBox _tabPaneBack;

  public Yaml _yaml;

  /* .yml constructor */
  public Project(Path ymlFile, Yaml yaml) {

    /* transfer yaml */
    _yaml = yaml;

    /* assigning fields */
    _ymlFile = ymlFile;
    _projName = _ymlFile.getFileName().toString()
            .substring(0, _ymlFile.getFileName().toString().length() - 4);
    _rootFolder = ymlFile.getParent();
    _rudisFolder = Paths.get(_rootFolder + "/" + PATH_TO_RUDI_FILES);
    log.info("Opening new project [" + _projName + "]");

    _runFile = Paths.get(_rootFolder + "/" + RUN_FILE);
    if (Files.exists(_runFile)) {
      log.info("run.sh has been found.");
    } else {
      _runFile = null;
      log.info("run.sh has not been found.");
    }

    _compileFile = Paths.get(_rootFolder + "/" + COMPILE_FILE);
    if (Files.exists(_compileFile)) {
      log.info("compile-script has been found.");
    } else {
      _compileFile = null;
      log.info("compile-script has not been found.");
    }
    retrieveLocRuleTreeView();

  }

  /* nullary constructor, used when only opening a directory */
  public Project() {}


  public void initProject() {
    Watch watch = new Watch();
    watch.createProjectWatch(this);
  }

  public void setDirectory(Path directory) {
    this._rootFolder = directory;
    this._projName = directory.getFileName().toString();
  }

//  public void clearProject() {
//    this = null;
//  }

  public void retrieveLocRuleTreeView() {
    _ruleLocFile = Paths.get(_rootFolder
            + "/" + _projName + RULE_LOCATION_SUFFIX);
    if (Files.exists(_ruleLocFile)) {
      log.info(_ruleLocFile + " has been found.");
    } else {
      _ruleLocFile = null;
      log.info(_projName + RULE_LOCATION_SUFFIX + " has not been found.");
    }
  }

  public Path getRootFolder() {
    return _rootFolder;
  }

  public Path getRudisFolder() {
    return _rudisFolder;
  }

  public String getProjectName() {
    return _projName;
  }

  public Path getCompileFile() {
    return _compileFile;
  }

  public Path getRunFile() {
    return _runFile;
  }

  public Path getRuleLocFile() {
    return _ruleLocFile;
  }

  public void setRuleTreeView(TreeView treeRules) {
    _ruleTreeView = treeRules;
  }

  public TreeView getRuleTreeView() {
    return _ruleTreeView;
  }

  public void setFileTreeView(TreeView treeFiles) {
    _fileTreeView = treeFiles;
  }

  public TreeView getFileTreeView() {
    return _fileTreeView;
  }

  public void setRudiHBox(RudiHBox tabPaneBack) {
    _tabPaneBack = tabPaneBack;
  }

  public RudiHBox getRudiHBox() {
    return _tabPaneBack;
  }

  public TreeView retrieveRuleLocMap() throws FileNotFoundException {
    LinkedHashMap<String, Object> load
            = (LinkedHashMap<String, Object>)
            _yaml.load(new FileReader(_ruleLocFile.toFile()));
    ArrayList<String> keys = new ArrayList<>(load.keySet());
    if (keys.size() != 1) {
      log.error("There is more than one main .rudi file.");
    }
    String rootKey = keys.get(0);
    ImportTreeItem root = new ImportTreeItem(rootKey, this);
    _ruleTreeView.setRoot(getNodes(rootKey, load, root));
    root.setExpanded(true);
    return _ruleTreeView;
  }

  public BasicTreeItem getNodes(String node, Map load, BasicTreeItem root) {
    for (String f : (Set<String>) ((LinkedHashMap) load.get(node)).keySet()) {

      // find another Map aka import / rule
      if (((LinkedHashMap) load.get(node)).get(f) instanceof Map) {
        LinkedHashMap tempMap
                = (LinkedHashMap) ((LinkedHashMap) load.get(node)).get(f);

        // find a new import
        if (tempMap.containsKey("%ImportWasInLine")) {
          ImportTreeItem item = new ImportTreeItem(f, this);
          root.getChildren().add(getNodes(f, (LinkedHashMap) load.get(node), item));
        }

        // find a new rule
        else {
          String correctedName = f;
          if (correctedName.contains("%")) {
            correctedName = slice_end(f, -2);
          }
          int inLine = (int) ((LinkedHashMap) ((LinkedHashMap) load.get(node)).get(f)).get("%InLine");
          RuleTreeItem item = new RuleTreeItem(correctedName,
                  inLine, this);
          root.getChildren().add(getNodes(f, (LinkedHashMap) load.get(node), item));
        }
      }

      // find an integer: f may be a rule and the value its line
      if (((LinkedHashMap) load.get(node)).get(f) instanceof Integer) {
        // .rudi file import
        if ("%ImportWasInLine".equals(f)) {
          // ignore for now
        } else if ("%InLine".equals(f)) {
          // ignore for now
        }
      }
      // find a String: the key-value-combination is an ERROR
      if (((LinkedHashMap) load.get(node)).get(f) instanceof String) {
        // ignore for now
      }

    }
    return root;
  }
}

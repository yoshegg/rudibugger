/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.project;

import de.dfki.rudibugger.ruleTreeView.RuleTreeItem;
import static de.dfki.rudibugger.Constants.*;
import de.dfki.rudibugger.WatchServices.Watch;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javafx.scene.control.TreeView;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import de.dfki.rudibugger.ruleTreeView.ImportTreeItem;

/**
 * This singleton contains all relevant information about the project
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Project {

  static Logger log = Logger.getLogger("rudiLog");
  public Yaml yaml;

  /* .yml constructor */
  private Project(File ymlFile) {
    _ymlFile = ymlFile;
    _projName = _ymlFile.getName()
            .substring(0, _ymlFile.getName().length() - 4);
    _rootFolder = ymlFile.getParentFile();
    _rudisFolder = new File(_rootFolder + "/" + PATH_TO_RUDI_FILES);
    log.info("Opening new project [" + _projName + "]");

    /* load Yaml to work with */
    yaml = new Yaml();

    _runFile = new File(_rootFolder.getPath() + "/" + RUN_FILE);
    if (_runFile.exists()) {
      log.info("run.sh has been found.");
    } else {
      _runFile = null;
      log.info("run.sh has not been found.");
    }

    _compileFile = new File(_rootFolder.getPath() + "/" + COMPILE_FILE);
    if (_compileFile.exists()) {
      log.info("compile-script has been found.");
    } else {
      _compileFile = null;
      log.info("compile-script has not been found.");
    }
    retrieveLocRuleTreeView();

  }

  public void retrieveLocRuleTreeView() {
    _ruleLocFile = new File(_rootFolder.getPath()
            + "/" + _projName + RULE_LOCATION_SUFFIX);
    if (_ruleLocFile.exists()) {
      log.info(_ruleLocFile.getName() + " has been found.");
    } else {
      _ruleLocFile = null;
      log.info(_projName + RULE_LOCATION_SUFFIX + " has not been found.");
    }
  }

  /* nullary constructor, used when only opening a directory */
  private Project() {}

  private File _compileFile;
  private File _runFile;
  private File _ymlFile;
  private File _rootFolder;
  private File _rudisFolder;
  private File _ruleLocFile;
  private LinkedHashMap _ymlMap;
  private TreeView _ruleTreeView;
  private String _projName;

  /* the only instance of the Project */
  private static Project ins = null;

  public static Project initProject(File ymlFile) {
    if (ins == null) {
      ins = new Project(ymlFile);
    }
    Watch watch = new Watch();
    watch.createProjectWatch(ins);
    return ins;
  }

  public static Project setDirectory(File directory) {
    if (ins == null) {
      ins = new Project();
      ins._rootFolder = directory;
      ins._projName = directory.getName();
    }
    return ins;
  }

  public static void clearProject() {
    ins = null;
  }

  public String getRootFolderPath() {
    return ins._rootFolder.getAbsolutePath();
  }

  public File getRudisFolderPath() {
    return ins._rudisFolder;
  }

  public String getProjectName() {
    return _projName;
  }

  public File getCompileFile() {
    return _compileFile;
  }

  public File getRunFile() {
    return _runFile;
  }

  public File getRuleLocFile() {
    return _ruleLocFile;
  }

  public void setRuleTreeView(TreeView treeRules) {
    _ruleTreeView = treeRules;
  }

  public TreeView getRuleTreeView() {
    return _ruleTreeView;
  }

  public TreeView retrieveRuleLocMap() throws FileNotFoundException {
    log.debug("Yaml file: " + yaml);
    log.debug("_ruleLocFile: " + _ruleLocFile);
    LinkedHashMap<String, Object> load
            = (LinkedHashMap<String, Object>)
            yaml.load(new FileReader(_ruleLocFile));
    log.debug("By Yaml loaded Map: " + load);
    ArrayList<String> keys = new ArrayList<>(load.keySet());
    if (keys.size() != 1) {
      log.error("There is more than one main .rudi file.");
    }
    String rootKey = keys.get(0);
    ImportTreeItem root = new ImportTreeItem(rootKey);
    _ruleTreeView.setRoot(getNodes(rootKey, load, root));
    root.setExpanded(true);
    return _ruleTreeView;
  }

  public ImportTreeItem getNodes(String node, Map load, ImportTreeItem root) {
    for (String f : (Set<String>) ((LinkedHashMap) load.get(node)).keySet()) {
      // find another Map aka import
      if (((LinkedHashMap) load.get(node)).get(f) instanceof Map) {
        ImportTreeItem item = new ImportTreeItem(f, root);
        root.getChildren().add(getNodes(f, (LinkedHashMap) load.get(node), item));
      }

      // find an integer: f may be a rule and the value its line
      if (((LinkedHashMap) load.get(node)).get(f) instanceof Integer) {
        if ("ImportWasInLine".equals(f)) {
          // ignore for now
        } else {
          RuleTreeItem item = new RuleTreeItem(f,
                  (int) ((LinkedHashMap) load.get(node)).get(f),
                  root);
          root.getChildren().add(item);
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

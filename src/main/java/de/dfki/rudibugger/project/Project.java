/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.rudibugger.project;

import static de.dfki.rudibugger.Constants.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

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
    String projName = _ymlFile.getName()
            .substring(0, _ymlFile.getName().length()-4);
    _rootFolder = ymlFile.getParentFile();
    _rudisFolder = new File(_rootFolder + "/" + PATH_TO_RUDI_FILES);
    log.info("Opening new project [" + projName + "]");

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

    _ruleLocFile = new File(_rootFolder.getPath()
              + "/" + projName + RULE_LOCATION_SUFFIX);
    if (_ruleLocFile.exists()) {
      log.info(_ruleLocFile.getName() + " has been found.");
    } else {
      _ruleLocFile = null;
      log.info(projName + RULE_LOCATION_SUFFIX + " has not been found.");
    }
  }

  /* nullary constructor */
  private Project() {}

  private File _compileFile;
  private File _runFile;
  private File _ymlFile;
  private File _rootFolder;
  private File _rudisFolder;
  private File _ruleLocFile;
  private LinkedHashMap _ymlMap;
  private LinkedHashMap _ruleLocMap;

  /* the only instance of the Project */
  private static Project ins = null;

  public static Project initProject(File ymlFile) {
    if (ins == null) {
      ins = new Project(ymlFile);
    }
    return ins;
  }

  public static Project setDirectory(File directory) {
    if (ins == null) {
      ins = new Project();
      ins._rootFolder = directory;
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
    String wEnding = ins._ymlFile.getName();
    String woEnding = wEnding.substring(0, wEnding.length()-4);
    return woEnding;
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

  public void retrieveRuleLocMap() throws FileNotFoundException {
    _ruleLocMap = new LinkedHashMap<>();
    Object load = yaml.load(new FileReader(_ruleLocFile));
    System.out.println(load);
  }
}

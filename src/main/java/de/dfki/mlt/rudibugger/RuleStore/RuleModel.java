/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleStore;

import de.dfki.mlt.rudimant.common.BasicInfo;
import de.dfki.mlt.rudimant.common.ImportInfo;
import de.dfki.mlt.rudimant.common.RuleInfo;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * The RuleModel is the part of rudibugger that keeps track of reading in the
 * RuleLoc.yml file. It is also responsible of refreshing its underlying data
 * structure, consisting of an Import called rootImport representing all the
 * rules of the project in a tree-like structure.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleModel {

  /** The logger of the the RuleModel */
  static Logger log = LoggerFactory.getLogger("RuleModel");

  /** The root path of the project */
  private Path _rudiPath;

  /** This set represents all of the used Imports path */
  private HashSet<Path> _importSet;

  /** This map maps ruleIds to paths and lines */
  private HashMap<Integer, RuleContainer> _idMap;


  /**
   * This field represents the root of the tree-like structure used to represent
   * all the rules of the RuleModel.
   */
  public ImportInfo rootImport;

  /** Constructs a new RuleModel */
  private RuleModel() {
    log.debug("An empty RuleModel has been created");
    _importSet = new HashSet();
    _idMap = new HashMap<>();
  }

  /**
   * Function to create a new RuleModel
   *
   * @return created RuleModel
   */
  public static RuleModel createNewRuleModel() {
    return new RuleModel();
  }

  /**
   * This function creates the tree-like structure for the very first time.
   *
   * @param ruleLocYml
   * @param rudiPath
   */
  public void readInRuleModel(Path ruleLocYml, Path rudiPath) {
    rootImport = readInRuleLocationFile(ruleLocYml);
    _rudiPath = rudiPath;
    extractImportsAndIds(rootImport);
  }

  public void updateRuleModel(Path ruleLocYml, Path rudiPath) {
    _importSet = new HashSet();
    _idMap = new HashMap<>();
    readInRuleModel(ruleLocYml, rudiPath);
  }

  /**
   * This function creates the tree-like structure used to represent all the
   * rules of the RuleModel.
   *
   * @param ruleLocFile the .yml file containing the map of all rules
   * @return
   */
  public ImportInfo readInRuleLocationFile(Path ruleLocFile) {
    ImportInfo ii;
    try {
      Yaml yaml = new Yaml();
      ii = (ImportInfo) yaml.load(new FileReader(ruleLocFile.toFile()));
    } catch (FileNotFoundException e) {
      log.error("Could not read in RuleLoc.yml");
      return null;
    }
    return ii;
  }

  /**
   * Container for line and Path of rules
   */
  public class RuleContainer {

    public RuleContainer(int line, Path path) {
      _line = line;
      _path = path;
    }

    private final int _line;
    private final Path _path;

    public int getLine() {return _line;}
    public Path getPath() {return _path;}

  }

  public HashSet<Path> getImportSet() {
    return _importSet;
  }

  public RuleContainer getRuleContainer(int id) {
    return _idMap.get(id);
  }

  /**
   * This function extracts the Imports from the current RuleModel.
   *
   * @param ii
   * @return
   */
//  private Set<Path> extractImportPaths(ImportInfo ii) {
//    HashSet<Path> set = new HashSet<>();
//    Path filePath = ii.getFilePath();
//    set.add(_rudiPath.resolve(filePath.subpath(1, filePath.getNameCount())));
//    extractImportPathsHelper(ii, set);
//    return set;
//  }

  /**
   * Helper function of extractImportPaths.
   *
   * @param ii
   * @param set
   */
//  private void extractImportPathsHelper(BasicInfo ii, HashSet set) {
//    if (!ii.getChildren().isEmpty()) {
//      for (BasicInfo child : ii.getChildren()) {
//        if (child instanceof ImportInfo) {
//          Path filePath = ((ImportInfo) child).getFilePath();
//          set.add(_rudiPath.resolve(filePath.subpath(1, filePath.getNameCount())));
//          extractImportPathsHelper(child, set);
//        }
//      }
//    }
//  }

  public void extractImportsAndIds(ImportInfo ii) {
    Path filePath = ii.getFilePath();
    _importSet.add(_rudiPath.resolve(filePath.subpath(1, filePath.getNameCount())));
    extractImportsAndIdsHelper(ii);
  }

  public void extractImportsAndIdsHelper(BasicInfo ii) {
    if (!ii.getChildren().isEmpty()) {
      for (BasicInfo child : ii.getChildren()) {

        if (child instanceof ImportInfo) {
          Path filePath = ((ImportInfo) child).getFilePath();
          _importSet.add(_rudiPath.resolve(filePath.subpath(1, filePath.getNameCount())));
        }

        if (child instanceof RuleInfo) {
          BasicInfo parent = child.getParent();
          while (!(parent instanceof ImportInfo)) {
            parent = parent.getParent();
          }
          Path parentPathIncomplete = ((ImportInfo) parent).getFilePath();
          Path parentPath = _rudiPath.resolve(parentPathIncomplete.subpath(1, parentPathIncomplete.getNameCount()));
          RuleInfo rule = (RuleInfo) child;
          _idMap.put(rule.getId(), new RuleContainer(rule.getLine(), parentPath));
        }

        extractImportsAndIdsHelper(child);
      }
    }
  }

}

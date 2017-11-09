/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RuleStore;

import de.dfki.mlt.rudimant.common.BasicInfo;
import de.dfki.mlt.rudimant.common.ImportInfo;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
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
  static Logger log = Logger.getLogger("RuleModel");

  /** This set represents all of the used Imports path */
  public Set<Path> importSet;

  /**
   * This field represents the root of the tree-like structure used to represent
   * all the rules of the RuleModel.
   */
  public ImportInfo rootImport;

  /** Constructs a new RuleModel */
  private RuleModel() {
    log.debug("An empty RuleModel has been created");
    importSet = new HashSet();
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
   */
  public void readInRuleModel(Path ruleLocYml) {
    rootImport = readInRuleLocationFile(ruleLocYml);
    importSet = extractImportPaths(rootImport);
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
   * This function extract the Imports from the current RuleModel
   *
   * @param ii
   * @return
   */
  private static Set<Path> extractImportPaths(ImportInfo ii) {
    HashSet<Path> set = new HashSet<>();
    set.add(ii.getFilePath());
    extractImportPathsHelper(ii, set);
    return set;
  }

  /**
   * Helper function of extractImportPaths
   *
   * @param ii
   * @param set
   */
  private static void extractImportPathsHelper(BasicInfo ii, HashSet set) {
    if (!ii.getChildren().isEmpty()) {
      for (BasicInfo child : ii.getChildren()) {
        if (child instanceof ImportInfo) {
          set.add(((ImportInfo) child).getFilePath());
          extractImportPathsHelper(child, set);
        }
      }
    }
  }
}

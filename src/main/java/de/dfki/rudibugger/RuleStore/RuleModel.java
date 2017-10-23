/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RuleStore;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * The RuleModel is the part of rudibugger that keeps track of reading in the
 * ~RuleLocation.yml file. It is also responsible of refreshing its underlying
 * data structure, consisting of an Import called rootImport representing all
 * the rules of the project in a tree-like structure.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleModel {

  /** The logger of the the RuleModel */
  static Logger log = Logger.getLogger("RuleModel");

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

  /** This set represents all of the used Imports path */
  public Set<Path> importSet;

  /**
   * This field represents the root of the tree-like structure used to represent
   * all the rules of the RuleModel.
   */
  public Import rootImport;

  /**
   * This function creates the tree-like structure for the very first time.
   *
   * @param ruleLocYml
  */
  public void readInRuleLocationFileFirstTime(Path ruleLocYml) {
    rootImport = readInRuleLocationFile(ruleLocYml);
  }

  /**
   * This function creates the tree-like structure used to represent all the
   * rules of the RuleModel.
   *
   * @param ruleLocYml the .yml file containing the map of all rules
   * @return
   */
  private Import readInRuleLocationFile(Path ruleLocYml) {

    LinkedHashMap<String, Object> loadedMap;

    /* read in the yml file */
    try {
      Yaml yaml = new Yaml();
      loadedMap
              = (LinkedHashMap<String, Object>)
              yaml.load(new FileReader(ruleLocYml.toFile()));
    } catch (FileNotFoundException e) {
      log.error("Could not read in ~RuleLocation.yml");
      return null;
    }

    /* verify that there is only one main .rudi file */
    ArrayList<String> keys = new ArrayList<>(loadedMap.keySet());
    if (keys.size() != 1) {
      log.error("There is more than one main .rudi file.");
    }

    /* get the name of the main .rudi file */
    String mainRudiName = keys.get(0);
    Import mainRudi = new Import(mainRudiName);
    mainRudi.setSource(Paths.get(mainRudiName + ".rudi"));


    /* remember usage of this import */
    importSet.add(mainRudi.getSource());

    /* variable to shorten expression to get to children */
    LinkedHashMap<String, Object> mainRudiChildren
            = (LinkedHashMap<String, Object>) loadedMap.get(mainRudiName);

    /* remove error messages before */
    ArrayList<String> errors = new ArrayList();
    for (String possibleError : mainRudiChildren.keySet()) {
      if (possibleError.startsWith("ERROR")) {
        errors.add(possibleError);
      }
    }
    for (String error : errors) {
      int errorLine = Integer.parseInt(error.substring(6));
      mainRudi.addError(errorLine, (String) mainRudiChildren.get(error));
      mainRudiChildren.remove(error);
    }

    /* retrieve and add all children */
    mainRudi.addChildren(getUnderlyingElements(mainRudiChildren, mainRudi), 0);

    return mainRudi;
  }

  private ArrayList<Object> getUnderlyingElements(
          LinkedHashMap<String, Object> children, Import currentImport) {

    /* the list that will be returned at the end */
    ArrayList<Object> returnList = new ArrayList();

    /* retrieve all children's names */
    Set<String> childrenNames = (Set<String>) children.keySet();

    /* iterate over the children and retrieve their underlying values */
    for (String child : childrenNames) {

      /* check if the child is an import */
      if (((LinkedHashMap) children.get(child))
              .containsKey("%ImportWasInLine")) {
        Import newImport = new Import(child);
        newImport.setSource(Paths.get(child + ".rudi"));
        newImport.setLine((Integer) ((LinkedHashMap) children
                .get(child)).get("%ImportWasInLine"));
        returnList.add(newImport);
        ((LinkedHashMap) children.get(child)).remove("%ImportWasInLine");

        /* remember usage of this import */
        importSet.add(newImport.getSource());

        /* remove error messages before */
        ArrayList<String> errors = new ArrayList();
        for (String possibleError : (Set<String>) ((LinkedHashMap) children
                .get(child)).keySet()) {
          if (possibleError.startsWith("ERROR")) {
            errors.add(possibleError);
          }
        }
        for (String error : errors) {
          int errorLine = Integer.parseInt(error.substring(6));
          String errorMessage = (String) ((LinkedHashMap)
                  ((LinkedHashMap) children).get(child)).get(error);
          newImport.addError(errorLine, errorMessage);
          ((LinkedHashMap) children.get(child)).remove(error);
        }

        /* check for rules and other imports in this import */
        if (((LinkedHashMap) children.get(child)).keySet().size() >= 1) {
          newImport.addChildren(getUnderlyingElements(((LinkedHashMap) children
                  .get(child)), newImport), 0);
        }
      }

      /* check if the child is a rule */
      if (((LinkedHashMap) children.get(child)).containsKey("%InLine")) {
        Rule newRule = new Rule(child);
        newRule.setLine((Integer) ((LinkedHashMap) children.get(child))
                .get("%InLine"));
        newRule.setSource(Paths.get(currentImport.getImportName())); // TODO THIS IS WRONG
        returnList.add(newRule);
        ((LinkedHashMap) children.get(child)).remove("%InLine");

        /* remove error messages before */
        ArrayList<String> errors = new ArrayList();
        for (String possibleError : (Set<String>) ((LinkedHashMap) children
                .get(child)).keySet()) {
          if (possibleError.startsWith("ERROR")) {
            errors.add(possibleError);
          }
        }
        for (String error : errors) {
          int errorLine = Integer.parseInt(error.substring(6));
          String errorMessage = (String) ((LinkedHashMap)
                  ((LinkedHashMap) children).get(child)).get(error);
          newRule.addError(errorLine, errorMessage);
          ((LinkedHashMap) children.get(child)).remove(error);
        }

        /* check for other rules in this rule */
        if (((LinkedHashMap) children.get(child)).keySet().size() >= 1) {
          newRule.addChildren(getUnderlyingElements(((LinkedHashMap) children
                  .get(child)), currentImport), 0);
        }
      }

    }
    return returnList;
  }

}

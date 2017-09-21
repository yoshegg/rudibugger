/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.ruleTreeView;

import java.io.File;
import javafx.scene.control.Label;

/**
 * This class represents every known fact about a rule:
 * name, source file, line and logging status
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleTreeItem extends BasicTreeItem {

  /* the name of the rule */
  private final Label _ruleName;

  /* the file the rule comes from */
  private File _sourceFile;

  /* the line in which the rule appears in the file */
  private final int _lineNumber;

  /**
   *
   * @param ruleName
   * @param lineNumber
   * @param parent
   */
  public RuleTreeItem(String ruleName, Integer lineNumber, BasicTreeItem parent) {
    super(ruleName, parent);

    _lineNumber = lineNumber;
    _ruleName = getLabel();
  }
}

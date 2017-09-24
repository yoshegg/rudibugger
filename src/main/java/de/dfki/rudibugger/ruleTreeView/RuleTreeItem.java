/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.ruleTreeView;

import java.io.File;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

/**
 * This class represents every known fact about a rule:
 * name, source file, line and logging status
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleTreeItem extends BasicTreeItem {

  /* the different icons used as indicator */
  static final String RULE_ICON_PATH
          = "file:src/main/resources/icons/RudiLogRuleStatus/";
  static Image imgAlways = new Image(RULE_ICON_PATH + "Always.png");
  static Image imgIfTrue = new Image(RULE_ICON_PATH + "IfTrue.png");
  static Image imgIfFalse = new Image(RULE_ICON_PATH + "IfFalse.png");
  static Image imgNever = new Image(RULE_ICON_PATH + "Never.png");
  static Image imgPartly = new Image(RULE_ICON_PATH + "Partly.png");

  /* the name of the rule */
  private final String _ruleName;

  /* the file the rule comes from */
  private File _sourceFile;

  /* the line in which the rule appears in the file */
  private final int _lineNumber;

  /* the constructor */
  public RuleTreeItem(String ruleName, Integer lineNumber) {
    super(ruleName);

    _lineNumber = lineNumber;
    _ruleName = ruleName;
  }

  /* returns the requested checkbox icon */
  public static Image getImage(int state) {
    switch (state) {
      case STATE_ALWAYS:
        return imgAlways;
      case STATE_IF_TRUE:
        return imgIfTrue;
      case STATE_IF_FALSE:
        return imgIfFalse;
      case STATE_NEVER:
        return imgNever;
      case STATE_PARTLY:
        return imgPartly;
      default:
        return null;
    }
  }
}

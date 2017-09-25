/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.ruleTreeView;

import de.dfki.rudibugger.project.Project;
import java.io.File;
import java.nio.file.Path;
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

  /* the line in which the rule appears in the file */
  private final int _lineNumber;

  /* the constructor */
  public RuleTreeItem(String ruleName, Integer lineNumber, Project proj) {
    super(ruleName, proj);

    _lineNumber = lineNumber;
    _ruleName = ruleName;

    /* the specific context menu for rules */
    _hb.setOnContextMenuRequested((e) -> {
      RuleContextMenu contextMenu = new RuleContextMenu(this);
      contextMenu.show(_hb, e.getScreenX(), e.getScreenY());
    });
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

  /* get source file */
  public Path getSourceFile() {
    return this.getParentImport().getFile();
  }

  /* get line number */
  public Integer getLineNumber() {
    return this._lineNumber;
  }
}

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.project;

import java.io.File;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * This class represents every known fact about a rule:
 * name, source file, line and logging status
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleTreeItem extends TreeItem {

  /* the different icons used as indicator */
  static final String RULE_ICON_PATH
          = "file:src/main/resources/icons/RudiLogRuleStatus/";
  static Image imgAlways  = new Image(RULE_ICON_PATH + "Always.png");
  static Image imgIfTrue = new Image(RULE_ICON_PATH + "IfTrue.png");
  static Image imgIfFalse = new Image(RULE_ICON_PATH + "IfFalse.png");
  static Image imgNever = new Image(RULE_ICON_PATH + "Never.png");

  /* the name of the rule */
  private final Label _ruleName;

  /* the file the rule comes from */
  private File _sourceFile;

  /* the line in which the rule appears in the file */
  private final int _lineNumber;

  /* the parent TreeItem */
  private TreeItem _parent;

  /* the icon to indicate the _state */
  private final ImageView stateIndicator;

  /* the state describes how the rule should be logged */
  private int _state;
  public static final int STATE_ALWAYS = 0;
  public static final int STATE_IF_TRUE = 1;
  public static final int STATE_IF_FALSE = 2;
  public static final int STATE_NEVER = 3;

  /**
   *
   * @param ruleName
   * @param lineNumber
   */
  public RuleTreeItem(String ruleName, Integer lineNumber) {
    super();
    HBox hb = new HBox();
    this.setValue(hb);

    _ruleName = new Label(ruleName);
    _lineNumber = lineNumber;

    stateIndicator = new ImageView();
    this.setToNever();

    hb.getChildren().add(stateIndicator);
    hb.getChildren().add(_ruleName);
    hb.setAlignment(Pos.CENTER_LEFT);

    hb.setOnContextMenuRequested(e -> {
           RuleContextMenu cm = new RuleContextMenu(this);
           cm.show(hb, e.getScreenX(), e.getScreenY());
        });
  }

  public void setToAlways() {
    stateIndicator.setImage(imgAlways);
    _state = STATE_ALWAYS;
  }

  public void setToIfTrue() {
    stateIndicator.setImage(imgIfTrue);
    _state = STATE_IF_TRUE;
  }

  public void setToIfFalse() {
    stateIndicator.setImage(imgIfFalse);
    _state = STATE_IF_FALSE;
  }

  public void setToNever() {
    stateIndicator.setImage(imgNever);
    _state = STATE_NEVER;
  }

  public void setState(int state) {
    switch (state) {
      case STATE_ALWAYS:
        setToIfTrue();
        break;
      case STATE_IF_TRUE:
        setToIfFalse();
        break;
      case STATE_IF_FALSE:
        setToNever();
        break;
      case STATE_NEVER:
        setToAlways();
        break;
    }
  }

  public int getState() {
    return _state;
  }

  /* used to test left click on rule. Probably removed later */
//  public void cycleTrough() {
//    switch (getState()) {
//      case STATE_ALWAYS:
//        setToIfTrue();
//        break;
//      case STATE_IF_TRUE:
//        setToIfFalse();
//        break;
//      case STATE_IF_FALSE:
//        setToNever();
//        break;
//      case STATE_NEVER:
//        setToAlways();
//        break;
//    }
//  }

}

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package ruleTreeView;

import java.util.HashSet;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class BasicTreeItem extends TreeItem {

  static Logger log = Logger.getLogger("rudiLog");


  /* the different icons used as indicator */
  static final String RULE_ICON_PATH
          = "file:src/main/resources/icons/RudiLogRuleStatus/";
  static Image imgAlways = new Image(RULE_ICON_PATH + "Always.png");
  static Image imgIfTrue = new Image(RULE_ICON_PATH + "IfTrue.png");
  static Image imgIfFalse = new Image(RULE_ICON_PATH + "IfFalse.png");
  static Image imgNever = new Image(RULE_ICON_PATH + "Never.png");
  static Image imgPartly = new Image(RULE_ICON_PATH + "Partly.png");

  /* the parent TreeItem */
  private final BasicTreeItem _parent;

  /* the label of the TreeItem */
  private final Label _label;

  /* the icon to indicate the _state */
  private final ImageView stateIndicator;

  /* the ContextMenu of the TreeItem */
  private RuleContextMenu contextMenu;

  /* the state describes how the rule(s) should be logged */
  private int _state;
  public static final int STATE_ALWAYS = 0;
  public static final int STATE_IF_TRUE = 1;
  public static final int STATE_IF_FALSE = 2;
  public static final int STATE_NEVER = 3;
  public static final int STATE_PARTLY = 9;

  public BasicTreeItem(String text, BasicTreeItem parent) {
    super();
    HBox hb = new HBox();
    this.setValue(hb);

    _parent = parent;
    _label = new Label(text);

    stateIndicator = new ImageView();
    this.setToNever();

    hb.getChildren().add(stateIndicator);
    hb.getChildren().add(_label);
    hb.setAlignment(Pos.CENTER_LEFT);

    RuleContextMenu contextMenu = new RuleContextMenu(this);
    contextMenu.initializeContextMenu();

    hb.setOnContextMenuRequested(e -> {
      contextMenu.retrieveState(_state);
      contextMenu.show(hb, e.getScreenX(), e.getScreenY());
    });
  }

  private void setToAlways() {
    stateIndicator.setImage(imgAlways);
    _state = STATE_ALWAYS;
  }

  private void setToIfTrue() {
    stateIndicator.setImage(imgIfTrue);
    _state = STATE_IF_TRUE;
  }

  private void setToIfFalse() {
    stateIndicator.setImage(imgIfFalse);
    _state = STATE_IF_FALSE;
  }

  private void setToNever() {
    stateIndicator.setImage(imgNever);
    _state = STATE_NEVER;
  }

  private void setToPartly() {
    stateIndicator.setImage(imgPartly);
    _state = STATE_PARTLY;
    if (_parent != null) {
      updateParentState();
    }
  }

  /**
   * This function sets the state of a rule or the entire .rudi file
   * and makes sure to refresh the TreeView.
   *
   * @param state a CONSTANT integer
   */
  public void setState(int state) {
    setState(state, true);
  }

  /**
   * sets the state of a BasicTreeItem
   *
   * @param state
   * @param goUp will only be set to false, if children are changed
   * after a parent has been changed first. Otherwise we get a
   * StackOverflowError
   */
  private void setState(int state, boolean goUp) {
    switch (state) {
      case STATE_ALWAYS:
        setToAlways();
        break;
      case STATE_IF_TRUE:
        setToIfTrue();
        break;
      case STATE_IF_FALSE:
        setToIfFalse();
        break;
      case STATE_NEVER:
        setToNever();
        break;
    }
    if (_parent != null && goUp) {
      updateParentState();
    }
    if (this instanceof ImportTreeItem) {
      this.getChildren().forEach((item) -> {
        ((BasicTreeItem) item).setState(state, false);
      });
    }
  }

  public int getState() {
    return _state;
  }

  public Label getLabel() {
    return _label;
  }

  /**
   * This function updates the state of parent nodes in the TreeView.
   *
   * If all children have the same state, the parent node will get the
   * same state and the grandparent will also be checked (and so on).
   * Otherwise the parent state will be set to 'partly' and all the other
   * parent nodes will be likewise.
   */
  public void updateParentState() {
    HashSet states = new HashSet();
    _parent.getChildren().forEach((item) -> {
      states.add((((BasicTreeItem) item)).getState());
    });
    if (states.size() == 1) {
      _parent.setState(_state);
    } else {
      _parent.setToPartly();
    }

  }
}

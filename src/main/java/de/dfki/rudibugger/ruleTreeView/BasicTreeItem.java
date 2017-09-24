/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.ruleTreeView;

import java.util.HashSet;
import javafx.collections.ObservableList;
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

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /* the label of the TreeItem */
  private final Label _label;

  /* the icon to indicate the _state */
  private final ImageView stateIndicator;

  /* the state describes how the rule(s) should be logged */
  private int _state;
  public static final int STATE_ALWAYS = 0;
  public static final int STATE_IF_TRUE = 1;
  public static final int STATE_IF_FALSE = 2;
  public static final int STATE_NEVER = 3;
  public static final int STATE_PARTLY = 9;

  /**
   * the HBox of the TreeItem, maybe it needs to be modified from
   * a more specific class
   */
  public HBox _hb;

  /* the constructor */
  public BasicTreeItem(String text) {
    super();

    /* initialise label and icon */
    _label = new Label(text);
    stateIndicator = new ImageView();
    this.setToNever();

    /* fill HBox */
    _hb = new HBox();
    this.setValue(_hb);
    _hb.getChildren().add(stateIndicator);
    _hb.getChildren().add(_label);
    _hb.setAlignment(Pos.CENTER_LEFT);


    RuleContextMenu contextMenu = new RuleContextMenu(this);
    contextMenu.initializeContextMenu();

    _hb.setOnContextMenuRequested(e -> {
      contextMenu.retrieveState(_state);
      contextMenu.show(_hb, e.getScreenX(), e.getScreenY());
    });
  }

  /********************************
   * SET STATUS FUNCTIONS - START *
   ********************************
   * TODO: The following functions may be removed later
   */
  private void setToAlways() {
    stateIndicator.setImage(getIcon(STATE_ALWAYS));
    _state = STATE_ALWAYS;
  }

  private void setToIfTrue() {
    stateIndicator.setImage(getIcon(STATE_IF_TRUE));
    _state = STATE_IF_TRUE;
  }

  private void setToIfFalse() {
    stateIndicator.setImage(getIcon(STATE_IF_FALSE));
    _state = STATE_IF_FALSE;
  }

  private void setToNever() {
    stateIndicator.setImage(getIcon(STATE_NEVER));
    _state = STATE_NEVER;
  }

  private void setToPartly() {
    stateIndicator.setImage(getIcon(STATE_PARTLY));
    _state = STATE_PARTLY;
  }

  /******************************
   * SET STATUS FUNCTIONS - END *
   ******************************
   * TODO: The following functions may be removed later
   */

  /**
   * returns the needed icon (file or checkbox)
   */
  private Image getIcon(Integer state) {
    if (this instanceof RuleTreeItem) {
      return RuleTreeItem.getImage(state);
    } else {
      return ImportTreeItem.getImage(state);
    }
  }

  /**
   * sets the state of a BasicTreeItem
   *
   * @param state
   */
  public void setState(int state) {
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
      case STATE_PARTLY:
        setToPartly();
        break;
    }

    /* unify underlying rules */
    if (this instanceof ImportTreeItem) {
      this.getAllChildren().forEach((item) -> {
        item.setStateHelper(_state);
      });
    }

    /* update parent's state and recursively the parent's parent */
    BasicTreeItem parent = this.getParentImport();
    while (parent != null) {
      HashSet<BasicTreeItem> allChildren = parent.getAllChildren();
      if (nodesHaveSameStates(allChildren)){
        parent.setStateHelper(state);
      } else {
        parent.setStateHelper(STATE_PARTLY);
      }
      parent = parent.getParentImport();
    }
  }

  /* this function only updates the state of a selected TreeItem */
  private void setStateHelper(int state) {
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
      case STATE_PARTLY:
        setToPartly();
        break;
    }
  }

  /* returns the private field _state */
  public int getState() {
    return _state;
  }

  /* returns the private field _label */
  public Label getLabel() {
    return _label;
  }

  /* returns all Children of a selected TreeItem */
  private HashSet<BasicTreeItem> getAllChildren() {
    HashSet<BasicTreeItem> allChildren = new HashSet<>();
    ObservableList children = this.getChildren();
    allChildren.addAll(children);
    children.forEach((item) -> {
      allChildren.addAll(((BasicTreeItem) item).getAllChildren());
    });
    return allChildren;
  }

  /* returns the file the TreeItem was imported from */
  private ImportTreeItem getParentImport() {
    TreeItem parent = this.getParent();
    if (parent == null) return null;
    while (! (parent instanceof ImportTreeItem)) {
      parent = parent.getParent();
    }
    return (ImportTreeItem) parent;
  }

  /* checks if a collection of TreeItems has the same state */
  private boolean nodesHaveSameStates(HashSet<BasicTreeItem> items) {
    HashSet states = new HashSet();
    items.forEach((i) -> {
      states.add((((BasicTreeItem) i)).getState());
    });
    return states.size() == 1;
  }
}

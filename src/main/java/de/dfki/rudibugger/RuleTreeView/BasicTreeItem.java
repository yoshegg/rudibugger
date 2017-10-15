/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RuleTreeView;

import static de.dfki.rudibugger.Constants.*;
import java.util.HashSet;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class BasicTreeItem extends TreeItem<HBox> {

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /* the label of the TreeItem */
  private final Label _label;

  /* the icon to indicate the _state */
  private final ImageView stateIndicator;

  /* the line this object was declared */
  private final IntegerProperty _line;

  /* the state describes how the rule(s) should be logged */
  private IntegerProperty _state;


  /**
   * the HBox of the TreeItem, maybe it needs to be modified from
   * a more specific class
   */
  public HBox _hb;


  /* the constructor */
  public BasicTreeItem(String text, Integer line) {
    super();

    /* initialise label and icon */
    _label = new Label(text);
    stateIndicator = new ImageView();
    _state = new SimpleIntegerProperty();
    this.setState(STATE_NEVER);

    _line = new SimpleIntegerProperty(line);


    /* disable doubleclick expand/collapse when clicking on the icon */
    stateIndicator.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {
      if (e.getClickCount() % 2 == 0 && e.getButton().equals(MouseButton.PRIMARY)) {
        e.consume();
      }
    });

    /* fill HBox */
    _hb = new HBox();
    this.setValue(_hb);
    _hb.getChildren().add(stateIndicator);
    _hb.getChildren().add(_label);
    _hb.setAlignment(Pos.CENTER_LEFT);
  }

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
  public final void setState(int state) {

    /* set state and adapt icon */
    _state.set(state);
    stateIndicator.setImage(getIcon(state));

    /* unify underlying rules */
    if (this instanceof ImportTreeItem) {
      this.getAllChildren().forEach((item) -> {
        item.setStateHelper(_state.get());
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

    /* set state and adapt icon */
    _state.set(state);
    stateIndicator.setImage(getIcon(state));
  }

  /* returns the private field _state */
  public IntegerProperty stateProperty() {
    return _state;
  }

  public IntegerProperty lineProperty() {
    return _line;
  }

  /* returns the private field _label */
  public Label getLabel() {
    return _label;
  }

  /* returns all Children of a selected TreeItem */
  protected HashSet<BasicTreeItem> getAllChildren() {
    HashSet<BasicTreeItem> allChildren = new HashSet<>();
    ObservableList children = this.getChildren();
    allChildren.addAll(children);
    children.forEach((item) -> {
      allChildren.addAll(((BasicTreeItem) item).getAllChildren());
    });
    return allChildren;
  }

  /* returns the ImportTreeItem the rule was imported from */
  protected ImportTreeItem getParentImport() {
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
      states.add((((BasicTreeItem) i)).stateProperty().get());
    });
    return states.size() == 1;
  }
}

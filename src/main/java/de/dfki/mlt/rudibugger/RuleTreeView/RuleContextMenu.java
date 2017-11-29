/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;
import static de.dfki.mlt.rudimant.common.Constants.*;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import static de.dfki.mlt.rudibugger.Constants.*;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * This is the context menu appearing when making a right click
 * on a rule in the lower TreeView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleContextMenu extends ContextMenu {

  /* the text of the different MenuItems */
  private final CheckMenuItem CMI_ALWAYS
          = new CheckMenuItem("Always log rule");
  private final CheckMenuItem CMI_IF_TRUE
          = new CheckMenuItem("Log rule if true");
  private final CheckMenuItem CMI_IF_FALSE
          = new CheckMenuItem("Log rule if false");
  private final CheckMenuItem CMI_NEVER
          = new CheckMenuItem("Never log rule");
  private final MenuItem CMI_ALWAYS_WITH_CHILDREN
          = new MenuItem("Always log rule and children");
  private final MenuItem CMI_IF_TRUE_WITH_CHILDREN
          = new MenuItem("Log rule including children if true");
  private final MenuItem CMI_IF_FALSE_WITH_CHILDREN
          = new MenuItem("Log rule including children if false");
  private final MenuItem CMI_NEVER_WITH_CHILDREN
          = new MenuItem("Never log rule and children");

  /* the clicked RuleMenuItem */
  private final RuleTreeItem _item;

  /* the constructor */
  public RuleContextMenu(RuleTreeItem item) {
    super();
    _item = item;

    initializeRuleLoggingMenuItems();
    retrieveState(_item.stateProperty().get());
  }

  /* set MenuItems' ActionEvents */
  private void initializeRuleLoggingMenuItems() {

    /* set open MenuItem */
    MenuItem openRule = new MenuItem("Open rule (line "
            + _item.getLine() + ")");
    openRule.setOnAction((ActionEvent e) -> {
      _item._model.requestTabOfRule(_item.getSourceFile(),
              _item.getLine());
    });
    SeparatorMenuItem sep = new SeparatorMenuItem();

    /* set actions when menu items are clicked */
    CMI_ALWAYS.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_ALWAYS);
    });
    CMI_IF_TRUE.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_IF_TRUE);
    });
    CMI_IF_FALSE.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_IF_FALSE);
    });
    CMI_NEVER.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_NEVER);
    });
    this.getItems().addAll(openRule, sep, CMI_ALWAYS, CMI_IF_TRUE, CMI_IF_FALSE, CMI_NEVER);

    /* if there are children, provide more options */
    if (! _item.getChildren().isEmpty()) {
      SeparatorMenuItem sep2 = new SeparatorMenuItem();
      Menu childrenMenu = new Menu("Children");
      CMI_ALWAYS_WITH_CHILDREN.setOnAction((ActionEvent e) -> {
        _item.setState(STATE_ALWAYS);
        unifyChildren(STATE_ALWAYS);
      });
      CMI_IF_TRUE_WITH_CHILDREN.setOnAction((ActionEvent e) -> {
        _item.setState(STATE_IF_TRUE);
        unifyChildren(STATE_IF_TRUE);
      });
      CMI_IF_FALSE_WITH_CHILDREN.setOnAction((ActionEvent e) -> {
        _item.setState(STATE_IF_FALSE);
        unifyChildren(STATE_IF_FALSE);
      });
      CMI_NEVER_WITH_CHILDREN.setOnAction((ActionEvent e) -> {
        _item.setState(STATE_NEVER);
        unifyChildren(STATE_NEVER);
      });
      childrenMenu.getItems().addAll(CMI_ALWAYS_WITH_CHILDREN,
              CMI_IF_TRUE_WITH_CHILDREN, CMI_IF_FALSE_WITH_CHILDREN,
              CMI_NEVER_WITH_CHILDREN);
      this.getItems().addAll(sep2, childrenMenu);
    }
  }

  /* get the state from the TreeItem */
  private void retrieveState(Integer state) {
    switch (state) {
      case STATE_ALWAYS:
        CMI_ALWAYS.setSelected(true);
        break;
      case STATE_IF_TRUE:
        CMI_IF_TRUE.setSelected(true);
        break;
      case STATE_IF_FALSE:
        CMI_IF_FALSE.setSelected(true);
        break;
      case STATE_NEVER:
        CMI_NEVER.setSelected(true);
        break;
    }
  }

  /* unify children rules */
  private void unifyChildren(Integer state) {
    ((BasicTreeItem) _item).getAllChildren().forEach((item) -> {
      item.setState(state);
    }
    );
  }
}
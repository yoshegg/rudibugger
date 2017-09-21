/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.ruleTreeView;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import static de.dfki.rudibugger.ruleTreeView.BasicTreeItem.STATE_ALWAYS;
import static de.dfki.rudibugger.ruleTreeView.BasicTreeItem.STATE_IF_FALSE;
import static de.dfki.rudibugger.ruleTreeView.BasicTreeItem.STATE_IF_TRUE;
import static de.dfki.rudibugger.ruleTreeView.BasicTreeItem.STATE_NEVER;
import static de.dfki.rudibugger.ruleTreeView.BasicTreeItem.STATE_PARTLY;

/**
 * This is the context menu appearing when making a right click
 * on a rule in the lower TreeView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleContextMenu extends ContextMenu {

  RadioMenuItem always;
  RadioMenuItem ifTrue;
  RadioMenuItem ifFalse;
  RadioMenuItem never;
  RadioMenuItem partly;

  private BasicTreeItem _item;

  public RuleContextMenu(BasicTreeItem item) {
    super();
    _item = item;
  }

  public void initializeContextMenu() {

    // set text of menu items
    always = new RadioMenuItem("always log rule");
    ifTrue = new RadioMenuItem("log rule if true");
    ifFalse = new RadioMenuItem("log rule if false");
    never = new RadioMenuItem("never log rule");

    // this menu item won't be added, but will be in the toggle group
    partly = new RadioMenuItem();

    // set actions when menu items are clicked
    always.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_ALWAYS);
    });
    ifTrue.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_IF_TRUE);
    });
    ifFalse.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_IF_FALSE);
    });
    never.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_NEVER);
    });

    // put all the menu items into one group so that only one can be selected
    ToggleGroup tG = new ToggleGroup();
    always.setToggleGroup(tG);
    ifTrue.setToggleGroup(tG);
    ifFalse.setToggleGroup(tG);
    never.setToggleGroup(tG);
    partly.setToggleGroup(tG);

    // add the menu items to the context menu
    this.getItems().addAll(always, ifTrue, ifFalse, never);
  }

  public void retrieveState(Integer state) {
    switch (state) {
      case STATE_ALWAYS:
        always.setSelected(true);
        break;
      case STATE_IF_TRUE:
        ifTrue.setSelected(true);
        break;
      case STATE_IF_FALSE:
        ifFalse.setSelected(true);
        break;
      case STATE_NEVER:
        never.setSelected(true);
        break;
      case STATE_PARTLY:
        partly.setSelected(true);
    }
  }
}
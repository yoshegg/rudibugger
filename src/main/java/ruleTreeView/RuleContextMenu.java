
/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package ruleTreeView;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import static ruleTreeView.BasicTreeItem.STATE_ALWAYS;
import static ruleTreeView.BasicTreeItem.STATE_IF_FALSE;
import static ruleTreeView.BasicTreeItem.STATE_IF_TRUE;
import static ruleTreeView.BasicTreeItem.STATE_NEVER;

/**
 * This is the context menu appearing when making a right click
 * on a rule in the lower TreeView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleContextMenu extends ContextMenu {

  public RuleContextMenu(BasicTreeItem item) {
    super();

    MenuItem always = new MenuItem("always log rule");
    MenuItem ifTrue = new MenuItem("log rule if true");
    MenuItem ifFalse = new MenuItem("log rule if false");
    MenuItem never = new MenuItem("never log rule");

    this.getItems().addAll(always, ifTrue, ifFalse, never);

    always.setOnAction(e -> {
      item.setState(STATE_ALWAYS);
    });

    ifTrue.setOnAction(e -> {
      item.setState(STATE_IF_TRUE);
    });

    ifFalse.setOnAction(e -> {
      item.setState(STATE_IF_FALSE);
    });

    never.setOnAction(e -> {
      item.setState(STATE_NEVER);
    });
  }

}

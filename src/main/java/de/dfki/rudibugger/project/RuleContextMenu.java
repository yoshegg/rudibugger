package de.dfki.rudibugger.project;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */

/**
 * This is the context menu appearing when making a right click
 * on a rule in the lower TreeView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleContextMenu extends ContextMenu {

  public RuleContextMenu(RuleTreeItem item) {
    super();

    MenuItem always = new MenuItem("always log rule");
    MenuItem ifTrue = new MenuItem("log rule if true");
    MenuItem ifFalse = new MenuItem("log rule if false");
    MenuItem never = new MenuItem("never log rule");

    this.getItems().addAll(always, ifTrue, ifFalse, never);

    always.setOnAction(e -> {
      item.setToAlways();
    });

    ifTrue.setOnAction(e -> {
      item.setToIfTrue();
    });

    ifFalse.setOnAction(e -> {
      item.setToIfFalse();
    });

    never.setOnAction(e -> {
      item.setToNever();
    });
  }

}

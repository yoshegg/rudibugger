/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RuleTreeViewState;

import de.dfki.rudibugger.RuleTreeView.BasicTreeItem;
import de.dfki.rudibugger.RuleTreeView.ImportTreeItem;
import java.util.HashSet;
import javafx.scene.control.TreeView;

/**
 * This class represents the current expansion state of the RuleTreeView and the
 * logging state of its elements.
 *
 * Unlike other classes representing the rule structure, this class will almost
 * never forget and is not sorted. The aim of this behavior is to be able to
 * know still know about temporarily removed Rules and Imports and to be able to
 * change the order of elements without losing their states.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleTreeViewState {

  private RuleTreeViewStateItem _root;

  /**
   * An instance of this private class represents a node in the tree.
   */
  private class RuleTreeViewStateItem {

    private final String label;
    private final Boolean isExpanded;
    private final Integer ruleLoggingState;
    private Boolean isImport = false;

    private HashSet<RuleTreeViewStateItem> children = new HashSet<>();

    public RuleTreeViewStateItem(String lab, Boolean expStat, int logStat) {
      label = lab;
      isExpanded = expStat;
      ruleLoggingState = logStat;
    }

    protected void addChildren(HashSet<RuleTreeViewStateItem> e) {
      if (!e.isEmpty()) {
        children.addAll(e);
      }
    }

    public boolean isImport() {
      return isImport;
    }

    public void isImport(Boolean x) {
      isImport = x;
    }

    public String getLabel() {
      return label;
    }
    
    public HashSet<RuleTreeViewStateItem> getChildren() {
      return children;
    }

    @Override
    public String toString() {
      return label + "(expanded: " + isExpanded + ", logState: "
              + ruleLoggingState + ")";
    }

  }

  /**
   * This function fills the RuleTreeViewState.
   *
   * @param tw
   * @return
   */
  public RuleTreeViewState retrieveTreeExpansionState(TreeView tw) {

    /* get root TreeItem of TreeView */
    BasicTreeItem root = (BasicTreeItem) tw.getRoot();

    /* the root of the RuleTreeViewState object */
    RuleTreeViewStateItem treeViewItem;
    treeViewItem = new RuleTreeViewStateItem(root.getLabel().getText(),
            root.isExpanded(), root.stateProperty().getValue());

    /* mark as Import */
    treeViewItem.isImport(true);

    /* create the children and add them */
    treeViewItem.addChildren(retrieveTreeExpansionStateHelper(root));

    /* set this item as root */
    _root = treeViewItem;

    /* return the RuleTreeViewState */
    return this;

  }

  private HashSet<RuleTreeViewStateItem>
          retrieveTreeExpansionStateHelper(BasicTreeItem tempItem) {

    /* the returned RuleTreeViewStateItems */
    HashSet<RuleTreeViewStateItem> set = new HashSet<>();

    /* iterate over the children */
    for (Object child : tempItem.getChildren()) {
      BasicTreeItem item = (BasicTreeItem) child;

      /* create the next RuleTreeViewStateItem */
      RuleTreeViewStateItem treeViewItem;
      treeViewItem = new RuleTreeViewStateItem(item.getLabel().getText(),
              item.isExpanded(), item.stateProperty().getValue());

      /* if it is an import, mark it */
      if (item instanceof ImportTreeItem) {
        treeViewItem.isImport(true);
      }

      /* create the children and add them */
      treeViewItem.addChildren(retrieveTreeExpansionStateHelper(item));

      /* add them to the returned set */
      set.add(treeViewItem);
    }
    return set;
  }

  /**
   * Pretty print the RuleTreeViewState
   *
   * @return pretty String
   */
  @Override
  public String toString() {
    if (_root == null) {
      return "RuleTreeViewState is empty";
    }

    String returnVal = "";
    returnVal += _root.toString() + "\n";

    String prefix = "  ";
    returnVal += toStringHelper(_root, prefix);

    return returnVal;
  }

  private String toStringHelper(RuleTreeViewStateItem e, String prefix) {
    String returnVal = "";
    for (RuleTreeViewStateItem x : e.getChildren()) {
      returnVal += prefix + x.toString() + "\n";
      prefix += "  ";
      returnVal += toStringHelper(x, prefix);
      prefix = prefix.substring(0, prefix.length() - 2);
    }
    return returnVal;
  }
}

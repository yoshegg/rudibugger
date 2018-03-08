/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import de.dfki.mlt.rudimant.common.BasicInfo;
import java.util.HashMap;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * This class represents the current expansion state of the RuleTreeView and the
 * logging state of its elements.
 *
 * Unlike other classes representing the rule structure, this class will almost
 * never forget and is not sorted. The aim of this behavior is to be able to
 * still know about temporarily removed Rules and Imports and to be able to
 * change the order of elements without losing their states.
 *
 * TODO: Find a convenient way to store the scroll position and selected item to
 * be able to restore it afterwards.
 *
 * This class is bloated because of the public getters and setters needed for
 * YAML.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleTreeViewState {

  public RuleTreeViewState() {}

  private RuleStateItem root;

  public RuleStateItem getRoot() { return root; }
  public void setRoot(RuleStateItem x) { this.root = x; }

  /**
   * This function fills the RuleTreeViewState.
   *
   * @param tw
   * @return
   */
  public RuleTreeViewState retrieveTreeState(TreeView tw) {

    /* get root TreeItem of TreeView */
    TreeItem<ImportInfoExtended> root = tw.getRoot();

    /* the root has not been created yet or the name does not match */
    if (this.root == null
      || !this.root.getLabel().equals(root.getValue().getLabel())) {
      this.root = new RuleStateItem(root.getValue().getLabel(),
        root.isExpanded(), root.getValue().getState());
      this.root.isImport(true);
    } else {
      this.root.updateRuleStateItem(
        root.isExpanded(), root.getValue().getState()
      );
    }

    /* create the children and add them */
    this.root.addChildren(retrieveTreeStateHelper(root, this.root));


    /* return the RuleTreeViewState */
    return this;

  }

  private HashMap<String, RuleStateItem>
    retrieveTreeStateHelper(TreeItem tempItem, RuleStateItem ruleItem) {

    /* the returned RuleTreeViewStateItems */
    HashMap<String, RuleStateItem> map = new HashMap<>();

    /* iterate over the children */
    for (Object child : tempItem.getChildren()) {
      RuleStateItem ruleStateItem;

      TreeItem<BasicInfo> item = (TreeItem) child;
      if (((TreeItem) child).getValue() instanceof RuleInfoExtended) {
        RuleInfoExtended itemValue = (RuleInfoExtended) ((TreeItem) child).getValue();
        /* is the child already known? if not: create a new one */
        if (ruleItem.getChildrenNames().contains(item.getValue().getLabel())) {
          ruleStateItem = ruleItem.getChild(item.getValue().getLabel());
          ruleStateItem.updateRuleStateItem(
                  item.isExpanded(), itemValue.getState()
          );
        } else {
          ruleStateItem = new RuleStateItem(item.getValue().getLabel(),
                  item.isExpanded(), itemValue.getState());

          /* if it is an import, mark it */
          if (item.getValue() instanceof ImportInfoExtended) {
            ruleStateItem.isImport(true);
          }
        }
      } else {
        ImportInfoExtended itemValue = (ImportInfoExtended) ((TreeItem) child).getValue();
        /* is the child already known? if not: create a new one */
        if (ruleItem.getChildrenNames().contains(item.getValue().getLabel())) {
          ruleStateItem = ruleItem.getChild(item.getValue().getLabel());
          ruleStateItem.updateRuleStateItem(
                  item.isExpanded(), itemValue.getState()
          );
        } else {
          ruleStateItem = new RuleStateItem(item.getValue().getLabel(),
                  item.isExpanded(), itemValue.getState());

          /* if it is an import, mark it */
          if (item.getValue() instanceof ImportInfoExtended) {
            ruleStateItem.isImport(true);
          }
        }
      }



      /* create the children and add them */
      ruleStateItem.addChildren(retrieveTreeStateHelper(item, ruleStateItem));

      /* add them to the returned set */
      map.put(item.getValue().getLabel(), ruleStateItem);
    }
    return map;
  }

  /**
   * This function expanses the TreeView and sets the rules' logging state.
   *
   * @param tw
   */
  public void setTreeState(TreeView tw) {

    /* get root TreeItem of TreeView */
    TreeItem<ImportInfoExtended> root = (TreeItem) tw.getRoot();

    /* has this item already appeared once? */
    if (root.getValue().getLabel().equals(this.root.getLabel())) {

      /* set the expansion state */
      root.setExpanded(this.root.getProps().getIsExpanded());

      /* iterate over the children */
      for (Object x : root.getChildren()) {
        TreeItem y = (TreeItem) x;
        setTreeStateHelper(y, this.root);
      }
    }

  }

  private void setTreeStateHelper(TreeItem<BasicInfo> obj, RuleStateItem item) {

    String lab = obj.getValue().getLabel();

    /* has this TreeItem already appeared once? */
    if (item.getChildrenNames().contains(lab)) {

      /* set the expansion state */
      obj.setExpanded(item.getChild(lab).getProps().getIsExpanded());

      /* if this is a rule, also set the log state */
      if (obj.getValue() instanceof RuleInfoExtended) {
        RuleInfoExtended rule = (RuleInfoExtended) obj.getValue();
        rule.setState(item.getChild(lab).getProps().getLoggingState());
      }

      /* iterate over the children */
      for (Object x : obj.getChildren()) {
        TreeItem<BasicInfo> y = (TreeItem) x;
        setTreeStateHelper(y, item.getChild(lab));
      }
    }
  }

  /**
   * Pretty print the RuleTreeViewState
   *
   * @return pretty String
   */
  @Override
  public String toString() {
    if (root == null) {
      return "RuleTreeViewState is empty";
    }

    String returnVal = "";
    returnVal += root.toString() + "\n";

    String prefix = "  ";
    returnVal += toStringHelper(root, prefix);

    return returnVal;
  }

  private String toStringHelper(RuleStateItem e, String prefix) {
    String returnVal = "";
    for (RuleStateItem x : e.getChildrenValues()) {
      returnVal += prefix + x.toString() + "\n";
      prefix += "  ";
      returnVal += toStringHelper(x, prefix);
      prefix = prefix.substring(0, prefix.length() - 2);
    }
    return returnVal;
  }
}

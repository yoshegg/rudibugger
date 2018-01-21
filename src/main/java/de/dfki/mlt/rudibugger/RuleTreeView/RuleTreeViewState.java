/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import java.util.HashMap;
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
   * The state of the RuleStateItem
   */
//  public class Properties {
//
//    public Properties() {}
//
//    private Boolean isExpanded;
//    private Integer loggingState;
//    private Boolean isImport = false;
//
//    public Properties(Boolean expStat, int logStat) {
//      isExpanded = expStat;
//      loggingState = logStat;
//    }
//
//    public Boolean getIsExpanded() { return isExpanded; }
//    public void setIsExpanded(Boolean isExpanded) {
//      this.isExpanded = isExpanded;
//    }
//
//    public Integer getLoggingState() { return loggingState; }
//    public void setLoggingState(Integer loggingState) {
//      this.loggingState = loggingState;
//    }
//
//    public Boolean getIsImport() { return isImport; }
//    public void setIsImport(Boolean isImport) { this.isImport = isImport; }
//  }

  /**
   * A node in the tree structure
   */
//  public class RuleStateItem {
//
//    public RuleStateItem() {}
//
//    /** The label of this RuleStateItem */
//    private String label;
//
//    public void setLabel(String x) { label = x; }
//    public String getLabel() { return label; }
//
//    /** A container containing the states of this RuleStateItem */
//    private Properties props;
//
//    public void setProps(Properties p) { props = p; }
//    public Properties getProps() { return props; }
//
//    /** The children of this RuleStateItem */
//    private HashMap<String, RuleStateItem> children = new HashMap<>();
//
//    public void setChildren(HashMap<String, RuleStateItem> m) { children = m; }
//    public HashMap<String, RuleStateItem> getChildren() { return children; }
//
//    /** Constructor of the RuleStateItem */
//    public RuleStateItem(String lab, Boolean expStat, int logStat) {
//      label = lab;
//      props = new Properties(expStat, logStat);
//    }
//
//    /** update the RuleStateItem */
//    public void updateRuleStateItem(Boolean expState, int logStat) {
//      props.setIsExpanded(expState);
//      props.setLoggingState((Integer) logStat);
//    }
//
//    /** add children to the RuleStateItem */
//    protected void addChildren(HashMap<String, RuleStateItem> e) {
//      if (!e.isEmpty()) {
//        e.forEach((key, val) ->
//          children.put(key, val));
//      }
//    }
//
//    public void isImport(Boolean x) { props.setIsImport(x); }
//
//    public Collection<RuleStateItem> getChildrenValues() {
//      return children.values();
//    }
//
//    public Set<String> getChildrenNames() {
//      return children.keySet();
//    }
//
//    public RuleStateItem getChild(String key) {
//      return children.get(key);
//    }
//
//    @Override
//    public String toString() {
//      return label + " (" + ((props.getIsImport()) ? "IMPORT, " : "")
//        + "expanded: " + props.getIsExpanded() + ", logState: "
//        + props.getLoggingState() + ")";
//    }
//
//  }

  /**
   * This function fills the RuleTreeViewState.
   *
   * @param tw
   * @return
   */
  public RuleTreeViewState retrieveTreeState(TreeView tw) {

    /* get root TreeItem of TreeView */
    BasicTreeItem root = (BasicTreeItem) tw.getRoot();

    /* the root has not been created yet or the name does not match */
    if (this.root == null
      || !this.root.getLabel().equals(root.getLabel().getText())) {
      this.root = new RuleStateItem(root.getLabel().getText(),
        root.isExpanded(), root.stateProperty().getValue());
      this.root.isImport(true);
    } else {
      this.root.updateRuleStateItem(
        root.isExpanded(), root.stateProperty().getValue()
      );
    }

    /* create the children and add them */
    this.root.addChildren(retrieveTreeStateHelper(root, this.root));


    /* return the RuleTreeViewState */
    return this;

  }

  private HashMap<String, RuleStateItem>
    retrieveTreeStateHelper(BasicTreeItem tempItem, RuleStateItem ruleItem) {

    /* the returned RuleTreeViewStateItems */
    HashMap<String, RuleStateItem> map = new HashMap<>();

    /* iterate over the children */
    for (Object child : tempItem.getChildren()) {
      BasicTreeItem item = (BasicTreeItem) child;

      /* is the child already known? if not: create a new one */
      RuleStateItem ruleStateItem;
      if (ruleItem.getChildrenNames().contains(item.getLabel().getText())) {
        ruleStateItem = ruleItem.getChild(item.getLabel().getText());
        ruleStateItem.updateRuleStateItem(
          item.isExpanded(), item.stateProperty().getValue()
        );
      } else {
        ruleStateItem = new RuleStateItem(item.getLabel().getText(),
                item.isExpanded(), item.stateProperty().getValue());

        /* if it is an import, mark it */
        if (item instanceof ImportTreeItem) {
          ruleStateItem.isImport(true);
        }
      }

      /* create the children and add them */
      ruleStateItem.addChildren(retrieveTreeStateHelper(item, ruleStateItem));

      /* add them to the returned set */
      map.put(item.getLabel().getText(), ruleStateItem);
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
    BasicTreeItem root = (BasicTreeItem) tw.getRoot();

    /* has this item already appeared once? */
    if (root.getLabel().getText().equals(this.root.getLabel())) {

      /* set the expansion state */
      root.setExpanded(this.root.getProps().getIsExpanded());

      /* iterate over the children */
      for (Object x : root.getChildren()) {
        BasicTreeItem y = (BasicTreeItem) x;
        setTreeStateHelper(y, this.root);
      }
    }

  }

  private void setTreeStateHelper(BasicTreeItem obj, RuleStateItem item) {

    String lab = obj.getLabel().getText();

    /* has this TreeItem already appeared once? */
    if (item.getChildrenNames().contains(lab)) {

      /* set the expansion state */
      obj.setExpanded(item.getChild(lab).getProps().getIsExpanded());

      /* if this is a rule, also set the log state */
      if (obj instanceof RuleTreeItem) {
        RuleTreeItem rule = (RuleTreeItem) obj;
        rule.setState(item.getChild(lab).getProps().getLoggingState());
      }

      /* iterate over the children */
      for (Object x : obj.getChildren()) {
        BasicTreeItem y = (BasicTreeItem) x;
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

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author christophe
 */
public class RuleStateItem {

  /**
   * The label of this RuleStateItem
   */
  private String label;

  /**
   * A container containing the states of this RuleStateItem
   */
  private Properties props;

  /**
   * The children of this RuleStateItem
   */
  private Map<String, RuleStateItem> children = new HashMap<>();


  public RuleStateItem() {}


  public void setLabel(String x) {
    this.label = x;
  }

  public String getLabel() {
    return label;
  }

  public void setProps(Properties p) {
    this.props = p;
  }

  public Properties getProps() {
    return props;
  }

  public void setChildren(HashMap<String, RuleStateItem> m) {
    this.children = m;
  }

  public Map<String, RuleStateItem> getChildren() {
    return children;
  }

  /**
   * Constructor of the RuleStateItem
   */
  public RuleStateItem(String lab, Boolean expStat, int logStat) {
    label = lab;
    props = new Properties(expStat, logStat);
  }

  /**
   * update the RuleStateItem
   */
  public void updateRuleStateItem(Boolean expState, int logStat) {
    props.setIsExpanded(expState);
    props.setLoggingState((Integer) logStat);
  }

  /**
   * add children to the RuleStateItem
   */
  protected void addChildren(HashMap<String, RuleStateItem> e) {
    if (!e.isEmpty()) {
      e.forEach((key, val)
        -> children.put(key, val));
    }
  }

  public void isImport(Boolean x) {
    props.setIsImport(x);
  }

  public Collection<RuleStateItem> getChildrenValues() {
    return children.values();
  }

  public Set<String> getChildrenNames() {
    return children.keySet();
  }

  public RuleStateItem getChild(String key) {
    return children.get(key);
  }

  @Override
  public String toString() {
    return label + " (" + ((props.getIsImport()) ? "IMPORT, " : "")
      + "expanded: " + props.getIsExpanded() + ", logState: "
      + props.getLoggingState() + ")";
  }

}

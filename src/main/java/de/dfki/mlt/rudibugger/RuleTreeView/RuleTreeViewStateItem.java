/*
 * The Creative Commons CC-BY-NC 4.0 License
 *
 * http://creativecommons.org/licenses/by-nc/4.0/legalcode
 *
 * Creative Commons (CC) by DFKI GmbH
 *  - Bernd Kiefer <kiefer@dfki.de>
 *  - Anna Welker <anna.welker@dfki.de>
 *  - Christophe Biwer <christophe.biwer@dfki.de>
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.dfki.mlt.rudibugger.RuleTreeView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A <code>RuleTreeViewStateItem</code> represents the look of a <code>TreeItem</code>
 * in <code>RuleTreeView</code>.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleTreeViewStateItem {

  /*****************************************************************************
   * FIELDS
   ****************************************************************************/

  /** *  Represents the label of this <code>RuleTreeViewStateItem</code>. */
  private String label;

  /** *  Represents a container describing this <code>RuleTreeViewStateItem</code>. */
  private RuleTreeViewStateItemProperties props;

  /** Contains the children of this RuleTreeViewStateItem. */
  private Map<String, RuleTreeViewStateItem> children = new HashMap<>();


  /*****************************************************************************
   * CONSTRUCTOR
   ****************************************************************************/

  /** Creates a new instance of <code>RuleStateItem</code>. */
  public RuleTreeViewStateItem(String lab, Boolean expStat, int logStat) {
    label = lab;
    props = new RuleTreeViewStateItemProperties(expStat, logStat);
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** *  Updates the <code>RuleTreeViewStateItem</code>. */
  public void updateRuleStateItem(Boolean expState, int logStat) {
    props.setIsExpanded(expState);
    props.setLoggingState((Integer) logStat);
  }

  /** *  Adds children to the <code>RuleTreeViewStateItem</code>. */
  public void addChildren(HashMap<String, RuleTreeViewStateItem> e) {
    if (!e.isEmpty()) {
      e.forEach((key, val)
        -> children.put(key, val));
    }
  }

  /**
   * @param x
   *        True, if the <code>RuleTreeViewStateItem</code> should be defined as an
   *        import, else false
   */
  public void isImport(Boolean x) {
    props.setIsImport(x);
  }

  /** @return The children's associated <code>RuleTreeViewStateItem</code> */
  public Collection<RuleTreeViewStateItem> getChildrenValues() {
    return children.values();
  }

  /** @return The children's names */
  public Set<String> getChildrenNames() {
    return children.keySet();
  }

  /**
   * @param key
   *        The <code>RuleTreeViewStateItem</code>'s name
   * @return The <code>RuleTreeViewStateItem</code> with the specified name
   */
  public RuleTreeViewStateItem getChild(String key) {
    return children.get(key);
  }

  @Override
  public String toString() {
    return label + " (" + ((props.getIsImport()) ? "IMPORT, " : "")
      + "expanded: " + props.getIsExpanded() + ", logState: "
      + props.getLoggingState() + ")";
  }


  /*****************************************************************************
   * YAML
   ****************************************************************************/

  /** Nullary constructor needed for YAML. */
  public RuleTreeViewStateItem() {}

  /** Label setter for YAML. */
  public void setLabel(String x) { this.label = x; }

  /** Label getter for YAML. */
  public String getLabel() { return label; }

  /** RuleTreeViewStateItemProperties setter for YAML. */
  public void setProps(RuleTreeViewStateItemProperties p) { this.props = p; }

  /** RuleTreeViewStateItemProperties getter for YAML. */
  public RuleTreeViewStateItemProperties getProps() { return props; }

  /** Children setter for YAML. */
  public void setChildren(HashMap<String, RuleTreeViewStateItem> m) {
    this.children = m;
  }

  /** Children getter for YAML. */
  public Map<String, RuleTreeViewStateItem> getChildren() { return children; }

}

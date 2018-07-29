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

package de.dfki.mlt.rudibugger.Project.RuleModel.State;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A <code>RuleStateItem</code> represents the look of a <code>TreeItem</code>
 * in <code>RuleTreeView</code>.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleStateItem {

  /*****************************************************************************
   * FIELDS
   ****************************************************************************/

  /** Represents the label of this <code>RuleStateItem</code>. */
  private String label;

  /** Represents a container describing this <code>RuleStateItem</code>. */
  private Properties props;

  /** Contains the children of this RuleStateItem. */
  private Map<String, RuleStateItem> children = new HashMap<>();


  /*****************************************************************************
   * CONSTRUCTOR
   ****************************************************************************/

  /** Creates a new instance of <code>RuleStateItem</code>. */
  public RuleStateItem(String lab, Boolean expStat, int logStat) {
    label = lab;
    props = new Properties(expStat, logStat);
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Updates the <code>RuleStateItem</code>. */
  public void updateRuleStateItem(Boolean expState, int logStat) {
    props.setIsExpanded(expState);
    props.setLoggingState((Integer) logStat);
  }

  /** Adds children to the <code>RuleStateItem</code>. */
  public void addChildren(HashMap<String, RuleStateItem> e) {
    if (!e.isEmpty()) {
      e.forEach((key, val)
        -> children.put(key, val));
    }
  }

  /**
   * @param x
   *        True, if the <code>RuleStateItem</code> should be defined as an
   *        import, else false
   */
  public void isImport(Boolean x) {
    props.setIsImport(x);
  }

  /** @return The children's associated <code>RuleStateItem</code> */
  public Collection<RuleStateItem> getChildrenValues() {
    return children.values();
  }

  /** @return The children's names */
  public Set<String> getChildrenNames() {
    return children.keySet();
  }

  /**
   * @param key
   *        The <code>RuleStateItem</code>'s name
   * @return The <code>RuleStateItem</code> with the specified name
   */
  public RuleStateItem getChild(String key) {
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
  public RuleStateItem() {}

  /** Label setter for YAML. */
  public void setLabel(String x) { this.label = x; }

  /** Label getter for YAML. */
  public String getLabel() { return label; }

  /** Properties setter for YAML. */
  public void setProps(Properties p) { this.props = p; }

  /** Properties getter for YAML. */
  public Properties getProps() { return props; }

  /** Children setter for YAML. */
  public void setChildren(HashMap<String, RuleStateItem> m) {
    this.children = m;
  }

  /** Children getter for YAML. */
  public Map<String, RuleStateItem> getChildren() { return children; }

}

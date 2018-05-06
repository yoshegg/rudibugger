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
 * TODO: DOCUMENTATION
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
  public void addChildren(HashMap<String, RuleStateItem> e) {
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

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

package de.dfki.mlt.rudibugger.project.ruleModel;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudimant.common.BasicInfo;
import static de.dfki.mlt.rudimant.common.Constants.*;
import de.dfki.mlt.rudimant.common.RuleInfo;
import java.nio.file.Path;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of <code>VOnDA</code>'s <code>RuleInfo</code>.
 * Contains a property to save the current logging status and other
 * functionality to change this RuleInfo.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleInfoExtended extends RuleInfo {

  static Logger log = LoggerFactory.getLogger(RuleInfoExtended.class);
  

  /* ***************************************************************************
   * FIELDS & PROPERTIES
   * **************************************************************************/

  /** Describes how the rules of this import are being logged. */
  private final IntegerProperty _state;

  /** Represents the Import containing this rule. */
  private final ImportInfoExtended _parentImport;


  /* ***************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   * **************************************************************************/

  /**
   * An extended version of <code>RuleInfo</code> containing information
   * about the logging status of its rules.
   *
   * @param   original
   *          An original RuleInfo retrieved from <code>VOnDA</code>.
   * @param   model
   *          The current DataModel of this rudibugger instance.
   * @param   parent
   *          The BasicInfo this RuleInfoExtended originates from.
   */
  public RuleInfoExtended(RuleInfo original, BasicInfo parent) {
    super();
    _label = original.getLabel();
    _line = original.getLine();
    _parent = parent;
    _state = new SimpleIntegerProperty(STATE_ALWAYS);
    _id = original.getId();
    _expr = original.getExpression();
    _baseTerms = original.getBaseterms();

    /* retrieve parent import and start listener*/
    if (_parent != null) {
      BasicInfo temp = _parent;
      while (! (temp instanceof ImportInfoExtended)) {
        temp = temp.getParent();
      }
      _parentImport = (ImportInfoExtended) temp;
    } else { _parentImport = null; }

  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /** Cycles through the different rule logging states */
  public void cycleThroughStates() {
    switch (this.getState()) {
      case STATE_ALWAYS:
        this.setState(STATE_IF_TRUE);
        break;
      case STATE_IF_TRUE:
        this.setState(STATE_IF_FALSE);
        break;
      case STATE_IF_FALSE:
        this.setState(STATE_NEVER);
        break;
      case STATE_NEVER:
        this.setState(STATE_ALWAYS);
        break;
    }
  }

  /**
   * Sets the state of this rule and all underlying rules to the given state.
   * Used by
   * {@link de.dfki.mlt.rudibugger.RuleTreeView.ImportInfoExtended#setAllChildrenStates(int) setAllChildrenStates}
   * method in a recursive way.
   *
   * @param state
   *        An integer {@linkplain de.dfki.mlt.rudimant.common constant}.
   */
  public void setAllChildrenStates(int state) {
    this.setState(state);
    this.getChildren().forEach((child) -> {
      ((RuleInfoExtended) child).setAllChildrenStates(state);
    });
  }


  /* ***************************************************************************
   * GETTERS & SETTERS
   * **************************************************************************/

  /**
   * Returns an Integer {@linkplain de.dfki.mlt.rudimant.common
   * constant} representing the logging state of this
   * <code>RuleInfoExtended</code>.
   *
   * @return An Integer {@linkplain de.dfki.mlt.rudimant.common
   *         constant}.
   */
  public int getState() { return _state.get(); }

  /**
   * Sets the rule logging state of this <code>RuleInfoExtended</code>.
   *
   * @param    i
   *           An Integer {@linkplain de.dfki.mlt.rudimant.common
   *           constant}.
   */
  public void setState(int i) { _state.setValue(i); }

  /**
   * Sets the given Integer {@linkplain de.dfki.mlt.rudimant.common
   * constant} as state of this <code>RuleInfoExtended</code>.
   *
   * @param value
   *        An Integer {@linkplain de.dfki.mlt.rudimant.common
   *        constant}
   */
  public void setStateProperty(int value) { _state.setValue(value); }

  /**
   * @return An <code>IntegerProperty</code> representing the logging state of
   * this <code>RuleInfoExtended</code>
   */
  public IntegerProperty stateProperty() { return _state; }

  /** @return The parent <code>ImportInfoExtended</code> */
  public ImportInfoExtended getParentImport() { return _parentImport; }

  /**
   * @return The parent's path of this <code>RuleInfoExtended</code> (the file
   * containing this rule)
   */
  public Path getSourceFile() {
    return ((ImportInfoExtended) _parentImport).getAbsolutePath();
  }

}

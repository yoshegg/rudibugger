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
import de.dfki.mlt.rudimant.common.IncludeInfo;
import java.nio.file.Path;
import java.util.HashSet;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of <code>VOnDA</code>'s <code>ImportInfo</code>.
 * Contains a property to save the current logging status of rules contained
 * in this import.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ImportInfoExtended extends IncludeInfo {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger(ImportInfoExtended.class);

  /** The current <code>DataModel</code>. */
//  private final DataModel _model;


  /* ***************************************************************************
   * FIELDS & PROPERTIES
   * **************************************************************************/

  /** Describes how the rules of this import are being logged. */
  private final IntegerProperty _state;

  /** Represents the associated file. */
  private final Path _file;

  /**
   * The ruleLoggingStates of all the children of this
   * <code>ImportInfoExtended</code> (e.g. other imports or rules).
   */
  private final HashSet<IntegerProperty> childStates = new HashSet<>();

  /** Describes whether or not this Import contains rules. */
  private boolean _containsRules;


  /* ***************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   * **************************************************************************/

  /**
   * An extended version of <code>ImportInfo</code> containing information
   * about the logging status of its rules.
   *
   * @param   original
   *          An original ImportInfo retrieved from <code>VOnDA</code>.
  TODO
   * @param   parent
   *          The parent of this Import, should already be extended.
   */
  public ImportInfoExtended(IncludeInfo original, Path rudiFolder,
                            BasicInfo parent) {
    super();
    _containsRules = false;
    _label = original.getLabel();
    _line = original.getLine();
    if (! (parent instanceof ImportInfoExtended) && (parent != null) )
      log.error("Tried to use a non-extended ImportInfo as parent.");
    _parent = parent;
    _errors = original.getErrors();
    _state = new SimpleIntegerProperty(STATE_ALWAYS);
//    _model = model;
    _file = rudiFolder.resolve(original.getFilePath()).normalize();
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /**
   * Sets the state of all underlying rules (including rules from subimports) to
   * the given state.
   *
   * @param state
   *        An integer {@linkplain de.dfki.mlt.rudimant.common constant}.
   */
  public void setAllChildrenStates(int state) {
    this.getChildren().forEach((child) -> {
      if (child instanceof RuleInfoExtended) {
        RuleInfoExtended ri = ((RuleInfoExtended) child);
        ri.setAllChildrenStates(state);
      } else {
        ImportInfoExtended ii = ((ImportInfoExtended) child);
        ii.setAllChildrenStates(state);
      }
    });
  }

  /**
   * Defines a listener to listen to changes of a given BasicInfo. This
   * BasicInfo will first be added to the children of this
   * <code>ImportInfoExtended</code> and then a listener will be created to
   * observe its ruleLoggingState.
   *
   * @param   bi
   *          A <code>ImportInfoExtended</code> or
   *          <code>RuleInfoExtended</code>.
   */
  public void addListener(BasicInfo bi) {
    IntegerProperty state;
    if (bi instanceof RuleInfoExtended)
      state = ((RuleInfoExtended) bi).stateProperty();
    else
      state = ((ImportInfoExtended) bi).stateProperty();

    childStates.add(state);
    state.addListener((cl, ov, nv) -> {
      Integer t = -1;
      for (IntegerProperty x : childStates) {
        if (x.getValue().equals(STATE_RULELESS)) continue;
        if ((t == -1) | (x.getValue().equals(t))) {
          t = x.getValue();
        } else {
          this.setStateProperty(STATE_PARTLY);
          return;
        }
      }
      this.setStateProperty(t);
    });
  }


  /* ***************************************************************************
   * GETTERS & SETTERS
   * **************************************************************************/

  /** @return The current <code>DataModel</code> */
//  public DataModel getModel() { return _model; }

  /**
   * Returns an Integer {@linkplain de.dfki.mlt.rudimant.common
   * constant} representing the logging state of this
   * <code>ImportInfoExtended</code>.
   *
   * @return An Integer {@linkplain de.dfki.mlt.rudimant.common
   *         constant}.
   */
  public int getState() { return _state.get(); }

  /**
   * Sets the given Integer {@linkplain de.dfki.mlt.rudimant.common
   * constant} as state of this <code>ImportInfoExtended</code>.
   *
   * @param value
   *        An Integer {@linkplain de.dfki.mlt.rudimant.common
   *        constant}.
   */
  public void setStateProperty(int value) { _state.setValue(value); }

  /**
   * Returns an <code>IntegerProperty</code> representing the logging state of
   * this <code>ImportInfoExtended</code>.
   *
   * @return An <code>IntegerProperty</code>.
   */
  public IntegerProperty stateProperty() { return _state; }

  /** @return The absolute path of this Import. */
  public Path getAbsolutePath() { return _file; }

  /** @Return True, if this Import contains rules, else false. */
  public boolean containsRules() { return _containsRules; }

  /** Sets the ImportInfoExtended to contain at least one rule. */
  public void setContainsRules() { _containsRules = true; }

}

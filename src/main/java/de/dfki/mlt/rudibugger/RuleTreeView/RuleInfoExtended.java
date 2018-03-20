/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

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

  /** The current <code>DataModel</code>. */
  protected final DataModel _model;

  /** Describes how the rules of this import are being logged. */
  private final IntegerProperty _state;

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
   * Returns an <code>IntegerProperty</code> representing the logging state of
   * this <code>RuleInfoExtended</code>.
   *
   * @return An <code>IntegerProperty</code>.
   */
  public IntegerProperty stateProperty() { return _state; }

  /**
   * An extended version of <code>RuleInfo</code> containing information
   * about the logging status of its rules.
   *
   * @param   original
   *          An original RuleInfo retrieved from <code>VOnDA</code>.
   * @param   model
   *          The current DataModel of this rudibugger instance.
   */
  public RuleInfoExtended(RuleInfo original, DataModel model,
                          BasicInfo parent) {
    super();
    _label = original.getLabel();
    _line = original.getLine();
    _parent = parent;
    _state = new SimpleIntegerProperty(STATE_NEVER);
    _model = model;
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

    /* TODO: This should probably be somewhere else. RuleModel maybe? */
    int id = original.getId();
    _state.addListener((o, ov, nv) -> {
      _model.vonda.client.setLoggingStatus(id, (int) nv);

      /* TODO: dito, internal logging purposes */
      String output = "";
      output += "SENT TO VONDA: CHANGED " + this._parentImport.getLabel()
              + ":" + this.getLabel() + " FROM " + ov + " TO " + nv;
      log.debug(output);
    });

  }

  /**
   * The Import containing this rule.
   */
  private final ImportInfoExtended _parentImport;

  /**
   * Returns the parent Import.
   *
   * @return A <code>ImportInfoExtended</code>.
   */
  public ImportInfoExtended getParentImport() { return _parentImport; }

  /**
   * Returns the parent's path of this <code>RuleInfoExtended</code> (the file
   * containing this rule).
   *
   * @return The file containing this rule.
   */
  public Path getSourceFile() {
    return ((ImportInfoExtended) _parentImport).getAbsolutePath();
  }

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
  protected void setAllChildrenStates(int state) {
    this.setState(state);
    this.getChildren().forEach((child) -> {
      ((RuleInfoExtended) child).setAllChildrenStates(state);
    });
  }
}

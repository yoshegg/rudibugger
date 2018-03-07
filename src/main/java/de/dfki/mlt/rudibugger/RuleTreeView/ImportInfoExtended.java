/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import de.dfki.mlt.rudibugger.DataModel;
import static de.dfki.mlt.rudimant.common.Constants.*;
import de.dfki.mlt.rudimant.common.ImportInfo;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Extension of <code>VOnDA</code>'s <code>ImportInfo</code>.
 * Contains a property to save the current logging status of rules contained
 * in this import.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ImportInfoExtended extends ImportInfo {

  /** The current <code>DataModel</code>. */
  protected final DataModel _model;

  /** Describes how the rules of this import are being logged. */
  private final IntegerProperty _state;

  /**
   * Returns an Integer {@linkplain de.dfki.mlt.rudimant.common
   * constant} representing the logging state of this <code>ImportInfo</code>.
   *
   * @return An Integer {@linkplain de.dfki.mlt.rudimant.common
   *         constant}
   */
  public int getState() { return _state.get(); }

  /**
   * Sets the given Integer {@linkplain de.dfki.mlt.rudimant.common
   * constant} as state of this <code>ImportInfo</code>.
   *
   * @param value
   *        An Integer {@linkplain de.dfki.mlt.rudimant.common
   *        constant}
   */
  public void setStateProperty(int value) { _state.setValue(value); }


  /**
   * An extended version of <code>ImportInfo</code> containing information
   * about the logging status of its rules.
   *
   * @param   original
   *          An original ImportInfo retrieved from <code>VOnDA</code>.
   * @param   model
   *          The current DataModel of this rudibugger instance.
   */
  public ImportInfoExtended(ImportInfo original, DataModel model) {
    super();
    _children = original.getChildren();
    _label = original.getLabel();
    _line = original.getLine();
    _parent = original.getParent();
    _errors = original.getErrors();
    _state = new SimpleIntegerProperty(STATE_NEVER);
    _model = model;
  }

}

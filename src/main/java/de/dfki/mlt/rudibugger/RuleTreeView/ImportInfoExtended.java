/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudimant.common.BasicInfo;
import static de.dfki.mlt.rudimant.common.Constants.*;
import de.dfki.mlt.rudimant.common.ErrorInfo;
import de.dfki.mlt.rudimant.common.ImportInfo;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
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
public class ImportInfoExtended extends ImportInfo {

  static Logger log = LoggerFactory.getLogger(ImportInfoExtended.class);

  /** The current <code>DataModel</code>. */
  protected final DataModel _model;

  /** Describes how the rules of this import are being logged. */
  private final IntegerProperty _state;

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

  /** TODO: Remove when changes in vonda have been accepted */
  private List<ErrorInfo> _errors;

  /** TODO: Remove when changes in vonda have been accepted */
  @Override
  public List<ErrorInfo> getErrors() { return _errors; }

  /**
   * The underlying file
   */
  private final Path _file;

  /**
   * Returns the absolute path of this Import.
   *
   * @return The absolute path of this Import.
   */
  public Path getAbsolutePath() { return _file; }

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
   * An extended version of <code>ImportInfo</code> containing information
   * about the logging status of its rules.
   *
   * @param   original
   *          An original ImportInfo retrieved from <code>VOnDA</code>.
   * @param   model
   *          The current DataModel of this rudibugger instance.
   * @param   parent
   *          The parent of this Import, should already be extended.
   */
  public ImportInfoExtended(ImportInfo original, DataModel model,
                            BasicInfo parent) {
    super();
    _label = original.getLabel();
    _line = original.getLine();
    if (! (parent instanceof ImportInfoExtended) && (parent != null) )
      log.error("Tried to use a non-extended ImportInfo as parent.");
    _parent = parent;
    _errors = original.getErrors();
    _state = new SimpleIntegerProperty(STATE_NEVER);
    _model = model;
    _file = _model.getRudiFolder().resolve(original.getFilePath()).normalize();

  }

  private final HashSet<IntegerProperty> childStates = new HashSet<>();
  private final HashSet<IntegerProperty> childImports = new HashSet<>();

  public void addRuleListener(RuleInfoExtended ri) {
    childStates.add(ri.stateProperty());
    ri.stateProperty().addListener((cl, ov, nv) -> {
      Integer t = -1;
      for (IntegerProperty x : childStates) {
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

  public void addImportListener(ImportInfoExtended ii) {
    childImports.add(ii.stateProperty());
    ii.stateProperty().addListener((cl, ov, nv) -> {
      Integer t = -1;
      for (IntegerProperty x : childImports) {
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

}
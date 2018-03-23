/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.DataModelAdditions;

import de.dfki.mlt.rudibugger.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality about project specific information.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ProjectConfiguration {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("rudiSave");

  /** The <code>DataModel</code> */
  private final DataModel _model;

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public ProjectConfiguration(DataModel model) {
    _model = model;
  }

}

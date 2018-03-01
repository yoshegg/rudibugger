/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import de.dfki.mlt.rudimant.common.RuleInfo;
import javafx.beans.property.IntegerProperty;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleInfoExtended extends RuleInfo {

  /** the state describes how the rule(s) should be logged */
  protected final IntegerProperty _state;

}

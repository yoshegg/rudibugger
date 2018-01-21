/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

/**
 *
 * @author christophe
 */
public class Properties {

  public Properties() {}

  Boolean isExpanded;
  Integer loggingState;
  Boolean isImport = false;

  public Properties(Boolean expStat, int logStat) {
    isExpanded = expStat;
    loggingState = logStat;
  }

  public Boolean getIsExpanded() {
    return isExpanded;
  }

  public void setIsExpanded(Boolean isExpanded) {
    this.isExpanded = isExpanded;
  }

  public Integer getLoggingState() {
    return loggingState;
  }

  public void setLoggingState(Integer loggingState) {
    this.loggingState = loggingState;
  }

  public Boolean getIsImport() {
    return isImport;
  }

  public void setIsImport(Boolean isImport) {
    this.isImport = isImport;
  }
}

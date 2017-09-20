/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package ruleTreeView;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ImportTreeItem extends BasicTreeItem {

  public ImportTreeItem(String importName, BasicTreeItem parent) {
    super(importName, parent);
  }

  public ImportTreeItem(String importName) {
    this(importName, null);
  }
}

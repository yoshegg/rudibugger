/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.tabs;

import javafx.scene.control.TabPane;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiTabPane extends TabPane {

  public RudiTabPane() {
    super();
    this.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
  }

}

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.Controller;

import de.dfki.rudibugger.mvc.DataModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class StatusBarController {

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /* the model */
  private DataModel model;

  public void initModel(DataModel model) {
    if (this.model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    this.model = model;

    statusBar.textProperty().bindBidirectional(model.statusBarProperty());

  }

  /* statusbar at the bottom */
  @FXML
  private Label statusBar;
}

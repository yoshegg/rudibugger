/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.Controller;

import de.dfki.rudibugger.mvc.DataModel;
import de.dfki.rudibugger.tabs.RudiHBox;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class EditorController {

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /* the model */
  private DataModel model;

  public void initModel(DataModel model) {
    if (this.model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    this.model = model;

    /* initialise the RudiHBox for the RudiTabPane(s) */
    tabPaneBack = new RudiHBox();
    tabPaneBack.fitToParentAnchorPane();
    tabAnchorPane.getChildren().add(tabPaneBack);

    /* TODO: REMOVE */
    model.tabPaneBack = tabPaneBack;
  }


  /******************************
   * The different GUI elements *
   ******************************/

  /* the ground AnchorPane of the HBox containing the tabPane(s) */
  @FXML
  private AnchorPane tabAnchorPane;

  /* the RudiHBox on top of tabAnchorPane */
  private RudiHBox tabPaneBack;

}

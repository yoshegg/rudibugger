/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.Controller;

import de.dfki.rudibugger.mvc.DataModel;
import de.dfki.rudibugger.mvc.Rule;
import de.dfki.rudibugger.project.RudiFileTreeItem;
import de.dfki.rudibugger.tabs.RudiTab;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SideBarController {

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /* the model */
  private DataModel model;

  public void initModel(DataModel model) {
    if (this.model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    this.model = model;

    /* TODO: REMOVE  */
    model.ruleTreeView = ruleTreeView;
    model.fileTreeView = fileTreeView;

    /* TODO: what should happen when a .rudi file is double clicked */
    fileTreeView.setOnMouseClicked((MouseEvent mouseEvent) -> {
      if (mouseEvent.getClickCount() == 2) {
        Object test = fileTreeView.getSelectionModel().getSelectedItem();
        if (test instanceof RudiFileTreeItem) {
          RudiFileTreeItem item = (RudiFileTreeItem) test;
          RudiTab tab = model.tabPaneBack.getTab(item.getFile());
        }
      }
    });

    /* TODO: this is probably wrong */
//    ruleTreeView.setCellFactory(param -> {
//      TreeCell<Rule> treeCell = new TreeCell<Rule>() {
//        @Override
//        protected void updateItem(Rule item, boolean empty) {
//          super.updateItem(item, empty);
//          if (item == null || empty) {
//            setText("");
//            setGraphic(null);
//            return;
//          }
//
//          setText(((Rule) item).getRuleName());
//        }
//      };
//      return treeCell;
//    });
  }

  /**
   * ****************************
   * The different GUI elements *
   *****************************
   */

  /* The TreeView showing the different rudi rules and imports */
  @FXML
  private TreeView ruleTreeView;

  /* The TreeView showing the content of the rudi folder (or root folder) */
  @FXML
  private TreeView fileTreeView;

}

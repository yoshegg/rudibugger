/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.Controller;

import static de.dfki.rudibugger.Constants.*;
import de.dfki.rudibugger.RuleStore.Import;
import de.dfki.rudibugger.DataModel;
import de.dfki.rudibugger.RudiList.RudiListViewCell;
import de.dfki.rudibugger.RudiList.RudiPath;
import de.dfki.rudibugger.RuleStore.Rule;
import de.dfki.rudibugger.RuleTreeView.BasicTreeItem;
import de.dfki.rudibugger.RuleTreeView.ImportTreeItem;
import de.dfki.rudibugger.RuleTreeView.RuleTreeItem;
import de.dfki.rudibugger.RuleTreeViewState.RuleTreeViewState;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;

/**
 * This controller manages the left part of rudibugger window:
 * the TreeView of files, the TreeView of rules and some buttons.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SideBarController {

  /** the logger of the SideBarController */
  static Logger log = Logger.getLogger("rudiLog");

  /** the DataModel */
  private DataModel model;

  /**
   * This function connects this controller to the DataModel
   *
   * @param model
   */
  public void initModel(DataModel model) {
    if (this.model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    this.model = model;



    model.projectStatusProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue o, Object oldVal,
              Object newVal) {
        switch ((int) newVal) {
          case PROJECT_OPEN:
            rudiListView.setItems(model.rudiList);
        }
      }
    });

    rudiListView.setCellFactory(value -> new RudiListViewCell());


    /* this Listener builds or modifies the TreeView, if the RuleModel
    was changed.*/
    model.ruleModelChangeProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue o, Object oldVal,
              Object newVal) {
        switch ((int) newVal) {
          case RULE_MODEL_NEWLY_CREATED:
            log.debug("RuleModel has been found.");
            log.debug("Building TreeView...");
            ruleTreeView.setRoot(buildTreeView(model));
            ruleTreeView.getRoot().setExpanded(true);
            log.debug("TreeView based on RuleModel has been built.");
            log.debug("Marking used .rudi files...");
            markFilesInRudiList();
            log.debug("Marked used .rudi files.");
            model.ruleModelChangeProperty().setValue(RULE_MODEL_UNCHANGED);
            break;
          case RULE_MODEL_CHANGED:
            log.debug("RuleModel has been modified.");
            log.debug("Adapting ruleTreeView");
            log.debug("FUNCTION TO BE IMPLEMENTED YET"); //TODO
            RuleTreeViewState ruleTreeViewState = new RuleTreeViewState();
            ruleTreeViewState.retrieveTreeExpansionState(ruleTreeView);
            ruleTreeView.setRoot(buildTreeView(model));
            log.debug("ruleTreeView has been adapted.");
            log.debug("Remarking used .rudi files...");
            markFilesInRudiList();
            log.debug("Remarked used .rudi files.");
            model.ruleModelChangeProperty().setValue(RULE_MODEL_UNCHANGED);
            break;
          case RULE_MODEL_REMOVED:
            log.debug("RuleModel has been resetted / removed");
            ruleTreeView.setRoot(null);
            // TODO: reset file view
            log.debug("GUI has been resetted.");
            model.ruleModelChangeProperty().setValue(RULE_MODEL_UNCHANGED);
            break;
          case RULE_MODEL_UNCHANGED:
            break;
          default:
            break;
        }
      }
    });



    /* open a new tab or select the already opened tab from the selected file */
    rudiListView.setOnMouseClicked((MouseEvent mouseEvent) -> {
      if (mouseEvent.getClickCount() == 2) {
        Object test = rudiListView .getSelectionModel().getSelectedItem();
        if (test instanceof RudiPath) {
          System.out.println("test");
//          RudiFileTreeItem item = (RudiFileTreeItem) test;
//          RudiTab tab = model.tabPaneBack.getTab(item.getFile());
        }
      }
    });
  }

  /**
   * This function is used to mark the files in the ListView according to
   * their state.
   */
  private void markFilesInRudiList() {
    for (RudiPath x : model.rudiList) {

      /* mark the main .rudi file */
      if (model.ruleModel.rootImport.getSource().getFileName().equals(
              x.getPath().getFileName())) {
        x._usedProperty().setValue(FILE_IS_MAIN);
        continue;
      }

      /* mark the other files */
      if (model.ruleModel.importSet.contains(x.getPath().getFileName())) {
        x._usedProperty().setValue(FILE_USED);
      } else {
        x._usedProperty().setValue(FILE_NOT_USED);
      }
    }

    /* let the cells reload according to their usage state */
    // TODO: https://stackoverflow.com/questions/14682881/binding-image-in-javafx
    rudiListView.refresh();
  }

  public static ImportTreeItem buildTreeView(DataModel model) {

    /* retrieve rootImport from given DataModel */
    Import rootImport = model.ruleModel.rootImport;

    /* build rootItem */
    ImportTreeItem rootItem = new ImportTreeItem(model.ruleModel.rootImport, model);

    /* bind rootItem's properties to the Import */
    rootImport.importNameProperty()
            .bindBidirectional(rootItem.getLabel().textProperty());

    /* iterate over rootImport's children and add them to the rootItem */
    for (Object obj : rootImport.childrenProperty()) {
      rootItem.getChildren().add(buildTreeViewHelper(obj, model));
    }

    /* return the rootItem */
    return rootItem;
  }

  private static BasicTreeItem buildTreeViewHelper(Object unknownObj,
          DataModel model) {

    /* the next object is an Import */
    if (unknownObj instanceof Import) {
      Import newImport = (Import) unknownObj;

      /* build newImportItem */
      ImportTreeItem newImportItem = new ImportTreeItem(newImport, model);

      /* bind newImportItem's properties to the Import */
      newImport.importNameProperty()
              .bindBidirectional(newImportItem.getLabel().textProperty());

      /* iterate over newImport's children and add them to the rootItem */
      for (Object obj : newImport.childrenProperty()) {
        newImportItem.getChildren().add(buildTreeViewHelper(obj, model));
      }
      return newImportItem;
    }

    /* the next object is a Rule */
    if (unknownObj instanceof Rule) {
      Rule newRule = (Rule) unknownObj;

      /* build newRuleItem */
      RuleTreeItem newRuleItem = new RuleTreeItem(newRule, model);

      /* bind newRuleItem's properties to the Rule */
      newRule.ruleNameProperty()
              .bindBidirectional(newRuleItem.getLabel().textProperty());
      newRule.ruleStateProperty()
              .bindBidirectional(newRuleItem.stateProperty());
      newRule.lineProperty()
              .bindBidirectional(newRuleItem.lineProperty());

      /* iterate over newRule's children and add them to the rootItem */
      for (Object obj : newRule.subRuleProperty()) {
        newRuleItem.getChildren().add(buildTreeViewHelper(obj, model));
      }
      return newRuleItem;
    }

    /* our new object is neither Rule nor Import */
    else {
      log.error("tried to read in an object that is not an Import or Rule.");
      return null;
    }
  }



  /**
   * ****************************
   * The different GUI elements *
   *****************************
   */

  /* The TreeView showing the different rudi rules and imports */
  @FXML
  private TreeView ruleTreeView;

  /* The ListView showing the content of the rudi folder */
  @FXML
  private ListView<RudiPath> rudiListView;

}

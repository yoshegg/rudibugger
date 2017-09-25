/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.tabs;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiHBox extends HBox {

  public RudiHBox() {
    _openTabs = new LinkedHashMap<>();
  }


  /* make sure to fit this HBox to its AnchorPane */
  public void fitToParentAnchorPane() {
    AnchorPane.setBottomAnchor(this, 0.0);
    AnchorPane.setTopAnchor(this, 0.0);
    AnchorPane.setLeftAnchor(this, 0.0);
    AnchorPane.setRightAnchor(this, 0.0);
  }


  /****************************************************
   * RudiTab store                                    *
   * all RudiTabs will be stored here to make sure    *
   * no one is opened twice                           *
   ****************************************************/

  /* the tab storage */
  private final LinkedHashMap<Path, RudiTab> _openTabs;

  /* check if tab for a file is already open */
  private boolean isFileOpen(Path id) {
    return _openTabs.containsKey(id);
  }


  /***************************************
   * RudiTab generation / retrieval      *
   * manages open TabPanes and open Tabs *
   ***************************************/

  /* get a new empty RudiTab */
  public RudiTab getNewEmptyTab() {
    RudiTab tab = new RudiTab();
    tab.setContent();
    RudiTabPane tabPane = getTabPane();
    tabPane.getTabs().add(tab);
    tabPane.getSelectionModel().select(tab);
    return tab;
  }

  /* get a RudiTab based on a file */
  public RudiTab getTab(Path file) {

    /* if the tab is already open, select it */
    if (isFileOpen(file)) {
      RudiTab tab = _openTabs.get(file);
      tab.getTabPane().getSelectionModel().select(tab);
      return tab;
    }

    /* if not, open it in the recent TabPane */
    else {
      RudiTab tab = new RudiTab(file);
      tab.setContent();
      RudiTabPane tabPane = getTabPane();
      tabPane.getTabs().add(tab);
      tabPane.getSelectionModel().select(tab);
      _openTabs.put(file, tab);
      return tab;
    }
  }

  /* get a RudiTab based on a file */
  public RudiTab getTabAtPosition(Path file, Integer line) {

    /* if the tab is already open, select it */
    if (isFileOpen(file)) {
      RudiTab tab = _openTabs.get(file);
      tab._codeArea.showParagraphPretty(line-1);
      tab._codeArea.moveTo(line-1, 0);
      tab.getTabPane().getSelectionModel().select(tab);
      return tab;
    }

    /* if not, open it in the recent TabPane */
    else {
      RudiTab tab = new RudiTab(file);
      tab.setContent();
      tab._codeArea.showParagraphPretty(line-1);
      tab._codeArea.moveTo(line-1, 0);
      RudiTabPane tabPane = getTabPane();
      tabPane.getTabs().add(tab);
      tabPane.getSelectionModel().select(tab);
      _openTabs.put(file, tab);
      return tab;
    }
  }

  /* close a Tab and remove it from openTabs */
  public void removeTabFromOpenTabs(RudiTab tab) {
    _openTabs.remove(tab.getFile());
  }

  /* creates a new RudiTabPane or gets the most recent */
  private RudiTabPane getTabPane() {
    ObservableList<Node> tabPanes = this.getChildren();
    if (tabPanes.isEmpty()) {
      return createNewRudiTabPane();
    } else if (tabPanes.size() == 1) {
      return (RudiTabPane) tabPanes.get(0);
    } else {
      // TODO: get most recent TabPane
      return null;
    }
  }

  /* creates a new RudiTabPane */
  private RudiTabPane createNewRudiTabPane() {
    RudiTabPane tabPane = new RudiTabPane();
    HBox.setHgrow(tabPane, Priority.ALWAYS);
    this.getChildren().add(tabPane);
    return tabPane;
  }
}

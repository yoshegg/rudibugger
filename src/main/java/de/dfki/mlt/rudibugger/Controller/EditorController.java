/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.Controller;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.TabManagement.TabStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class EditorController {

  /* the logger */
  static Logger log = LoggerFactory.getLogger("rudiLog");

  /* the model */
  private DataModel _model;

  public void initModel(DataModel model) {
    if (_model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    _model = model;

    /* initialise the TabStore */
    tabStore = new TabStore(tabBox);

    /* this listener waits for tab requests: open or switch to */
    _model.requestedFileProperty().addListener((arg, oldVal, newVal) -> {
      if (newVal != null) {
        tabStore.openTab(newVal);
      }
    _model.requestedFileProperty().setValue(null);
    });

    /* this listener waits for tab requests: close */
    _model.requestedCloseTabProperty().addListener((arg, oldVal, newVal) -> {
      if (newVal != null) {
        tabStore.closeTab(newVal);
      }
    });

    /* this listener represents the current active tab */
    _model.selectedTabProperty().bindBidirectional(
            tabStore.currentTabProperty());

    /* this listener adds a ListView to the editorSplitPane if connection to
     * rudimant has been established.
     */
    _model.connectedToRudimantProperty().addListener((arg, oldVal, newVal) -> {
      if (newVal == true) {
        System.out.println("rudimant is connected");

        ruleLoggingListView = new ListView();
        ruleLoggingListView.setItems(ruleLoggingList);

        AnchorPane ap = new AnchorPane();
        editorSplitPane.getItems().add(1, ap);
        AnchorPane.setTopAnchor(ruleLoggingListView, 0.0);
        AnchorPane.setRightAnchor(ruleLoggingListView, 0.0);
        AnchorPane.setLeftAnchor(ruleLoggingListView, 0.0);
        AnchorPane.setBottomAnchor(ruleLoggingListView, 0.0);
        ap.getChildren().add(ruleLoggingListView);
        editorSplitPane.setDividerPositions(0.5);

      } else {
        System.out.println("rudimant is disconnected.");
        // TODO: Remove SplitPane and ListView
      }
    });

    /* this listener adds new output the the ruleLoggineList */
    _model.rudiLogOutputProperty().addListener((arg, oldVal, newVal) -> {
      if (newVal != null) {
        Platform.runLater(() -> {
          ruleLoggingList.add(newVal);
        });
      }
    });
  }

  /*****************************************************************************
   * The Tab Management
   ****************************************************************************/

  private TabStore tabStore;


  /*****************************************************************************
   * The different GUI elements
   ****************************************************************************/

  /** ListView for ruleLogging */
  private ListView ruleLoggingListView;

  /** The underlying ObservableList of the ruleLoggingList */
  private final ObservableList ruleLoggingList
          = FXCollections.observableArrayList();

  /** The HBox containing the tabPane(s) */
  @FXML
  private HBox tabBox;

  /** The underlying SplitPane to which the ruleLogger will be added */
  @FXML
  private SplitPane editorSplitPane;

}

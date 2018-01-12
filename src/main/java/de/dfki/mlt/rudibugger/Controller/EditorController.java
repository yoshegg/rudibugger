/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.Controller;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.RPC.EvaluatedCellFactory;
import de.dfki.mlt.rudibugger.RPC.LabelCellFactory;
import de.dfki.mlt.rudibugger.RPC.LogData;
import de.dfki.mlt.rudibugger.RPC.LogData.StringPart;
import de.dfki.mlt.rudibugger.RPC.TimestampCellFactory;
import de.dfki.mlt.rudibugger.TabManagement.TabStore;
import java.util.ArrayList;
import java.util.Date;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    /* this listener adds a TableView to the editorSplitPane if connection to
     * rudimant has been established.
     */
    _model.connectedToRudimantProperty().addListener((arg, oldVal, newVal) -> {
      if (newVal == true) {
        log.info("Rudimant successfully connected to rudibugger.");

        /* create new TableView */
        ruleLoggingTableView = new TableView();

        /* define columns */
        _labelColumn = new TableColumn<>();
        _labelColumn.setText("Label");
        _labelColumn.setPrefWidth(180.0);
        _evaluatedColumn = new TableColumn<>();
        _evaluatedColumn.setText("Evaluated");
        _timeColumn = new TableColumn<>();
        _timeColumn.setText("Time");
        ruleLoggingTableView.getColumns().addAll(
                _timeColumn, _labelColumn, _evaluatedColumn);

        /* setCellValueFactories */
        _labelColumn.setCellValueFactory(value -> value.getValue().label);
        _evaluatedColumn.setCellValueFactory(value -> value.getValue().evaluated);
        _timeColumn.setCellValueFactory(value -> value.getValue().timestamp);

        /* setCellFactories */
        _labelColumn.setCellFactory(value -> new LabelCellFactory());
        _evaluatedColumn.setCellFactory(value -> new EvaluatedCellFactory());
        _timeColumn.setCellFactory(value -> new TimestampCellFactory());

        /* set items of TableView */
        ruleLoggingTableView.setItems(ruleLoggingList);

        /* set cell height of TableView */
        ruleLoggingTableView.setFixedCellSize(25);

        /* set column resizing policies */
        ruleLoggingTableView.widthProperty().addListener((cl, ov, nv) -> {
          adaptTableViewColumns();
        });
        _timeColumn.widthProperty().addListener((cl, ov, nv) -> {
          adaptTableViewColumns();
        });
        _evaluatedColumn.widthProperty().addListener((cl, ov, nv) -> {
          adaptTableViewColumns();
        });
        _labelColumn.widthProperty().addListener((cl, ov, nv) -> {
          adaptTableViewColumns();
        });

        /* fit into AnchorPane */
        AnchorPane ap = new AnchorPane();
        editorSplitPane.getItems().add(1, ap);
        AnchorPane.setTopAnchor(ruleLoggingTableView, 0.0);
        AnchorPane.setRightAnchor(ruleLoggingTableView, 0.0);
        AnchorPane.setLeftAnchor(ruleLoggingTableView, 0.0);
        AnchorPane.setBottomAnchor(ruleLoggingTableView, 0.0);
        ap.getChildren().add(ruleLoggingTableView);
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
   * METHODS
   ****************************************************************************/

  public void adaptTableViewColumns() {
    Double prefWidth = ruleLoggingTableView.widthProperty().getValue()
                  - _labelColumn.getWidth() - _timeColumn.getWidth() - 2;
    _evaluatedColumn.setPrefWidth(prefWidth);
  }

  /*****************************************************************************
   * The Tab Management
   ****************************************************************************/

  private TabStore tabStore;


  /*****************************************************************************
   * The different GUI elements
   ****************************************************************************/

  /** ListView for ruleLogging */
  private TableView ruleLoggingTableView;

  /** The underlying ObservableList of the ruleLoggingList */
  private final ObservableList<LogData> ruleLoggingList
          = FXCollections.observableArrayList();

  /** The HBox containing the tabPane(s) */
  @FXML
  private HBox tabBox;

  /** The underlying SplitPane to which the ruleLogger will be added */
  @FXML
  private SplitPane editorSplitPane;

  /* Columns */
  private TableColumn<LogData, StringPart> _labelColumn;
  private TableColumn<LogData, ArrayList<StringPart>> _evaluatedColumn;
  private TableColumn<LogData, Date> _timeColumn;

}

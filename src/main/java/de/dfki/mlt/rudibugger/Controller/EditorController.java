/*
 * The Creative Commons CC-BY-NC 4.0 License
 *
 * http://creativecommons.org/licenses/by-nc/4.0/legalcode
 *
 * Creative Commons (CC) by DFKI GmbH
 *  - Bernd Kiefer <kiefer@dfki.de>
 *  - Anna Welker <anna.welker@dfki.de>
 *  - Christophe Biwer <christophe.biwer@dfki.de>
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.dfki.mlt.rudibugger.Controller;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.RPC.EvaluatedCellFactory;
import de.dfki.mlt.rudibugger.RPC.LabelCellFactory;
import de.dfki.mlt.rudibugger.RPC.LogData;
import de.dfki.mlt.rudibugger.RPC.LogData.DatePart;
import de.dfki.mlt.rudibugger.RPC.LogData.StringPart;
import de.dfki.mlt.rudibugger.RPC.TimestampCellFactory;
import static de.dfki.mlt.rudibugger.RPC.TimestampCellFactory.dt;
import de.dfki.mlt.rudibugger.TabManagement.TabStore;
import java.nio.file.Path;
import java.util.ArrayList;
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

    /* this listener represents all the currently open tabs */
    _model.openTabsProperty().bindBidirectional(
            tabStore.openTabsProperty());

    /* this listener adds a TableView to the editorSplitPane if connection to
     * rudimant has been established.
     */
    _model.vonda.connectedProperty().addListener((arg, oldVal, newVal) -> {
      if (newVal == true) {
        log.info("VOnDA successfully connected to rudibugger.");

        //TODO: Make sure the following is only happening once.
        /* create new TableView */
        ruleLoggingTableView = new TableView();

        /* define columns */
        _labelColumn = new TableColumn<>();
        _labelColumn.setText("Label");
        _labelColumn.setPrefWidth(180.0);
        _evaluatedColumn = new TableColumn<>();
        _evaluatedColumn.setText("Evaluated");
//        _timeColumn = new TableColumn<>();
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
        _timeColumn.setCellFactory(value -> new TimestampCellFactory(
                 _model.globalConf.timeStampIndexProperty().get())
        );

        /* set comparators */
        _timeColumn.setComparator((x, y) -> (
                dt.format(x.date) + Integer.toString(x.counter))
                .compareToIgnoreCase(
                dt.format(y.date) + Integer.toString(y.counter))
        );
        _labelColumn.setComparator((x, y) -> (
                x.content.compareTo(y.content))
        );

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


        /* jump to rule when clicked */
        ruleLoggingTableView.setOnMousePressed(e -> {
          if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
            int ruleId = ((LogData) ruleLoggingTableView.getSelectionModel()
                    .getSelectedItem()).getRuleId();
            Path importToOpen =
                    _model.ruleModel.getRule(ruleId).getSourceFile();
            int lineToOpen = _model.ruleModel.getRule(ruleId).getLine();
            _model.rudiLoad.openRule(importToOpen, lineToOpen);
          }
        });

      } else {
        log.info("Connection to VOnDA has been closed.");
      }
    });

    /* this listener adds new output the the ruleLoggineList */
    _model.vonda.logOutputProperty().addListener((arg, oldVal, newVal) -> {
      if (newVal != null) {
        Platform.runLater(() -> {
          ruleLoggingList.add(0, newVal);
          ruleLoggingTableView.sort();
        });
      }
    });

    /* this listener updates the timeStampIndex setting in the tableView */
    _model.globalConf.timeStampIndexProperty().addListener(cl -> {
        updateTimeStampIndexSetting();
    });

  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  public void adaptTableViewColumns() {
    Double correctionValue = 18.0;
    Double prefWidth = ruleLoggingTableView.widthProperty().getValue()
            - _labelColumn.getWidth() - _timeColumn.getWidth()
            - correctionValue;
    _evaluatedColumn.setPrefWidth(prefWidth);
  }

  public void updateTimeStampIndexSetting() {
    boolean timeStampIndexSetting
      = _model.globalConf.timeStampIndexProperty().get();
      _timeColumn.setCellFactory(value ->
            new TimestampCellFactory(timeStampIndexSetting)
        );
    if (timeStampIndexSetting) {
      _timeColumn.setPrefWidth(126.24658203125);
    } else {
      _timeColumn.setPrefWidth(105.3310546875);
    }
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
  private TableColumn<LogData, DatePart> _timeColumn = new TableColumn<>();

}

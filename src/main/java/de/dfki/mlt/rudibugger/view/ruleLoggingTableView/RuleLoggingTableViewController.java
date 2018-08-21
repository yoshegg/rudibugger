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
package de.dfki.mlt.rudibugger.view.ruleLoggingTableView;

import de.dfki.mlt.rudibugger.GlobalConfiguration;
import de.dfki.mlt.rudibugger.editor.Editor;
import de.dfki.mlt.rudibugger.rpc.LogData;
import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudibugger.project.VondaRuntimeConnection;
import de.dfki.mlt.rudibugger.project.ruleModel.RuleInfoExtended;
import de.dfki.mlt.rudibugger.project.ruleModel.RuleModel;
import static de.dfki.mlt.rudibugger.view.ruleLoggingTableView.TimestampCellFactory.dt;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleLoggingTableViewController {

  static Logger log = LoggerFactory.getLogger("ruleLoggingTable");

  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  private GlobalConfiguration _globalConf;
  private Editor _editor;
  private VondaRuntimeConnection _vonda;
  private Project _project;

  /** Represents the list of logged rules. */
  private final ObservableList<LogData> ruleLoggingList
          = FXCollections.observableArrayList();


  /* ***************************************************************************
   * GUI ELEMENTS
   * **************************************************************************/

  /** Shows the logged rules as a table. */
  private TableView _ruleLoggingTableView;

  /* Columns */
  private final TableColumn<LogData, LogData.StringPart> _labelColumn
    = new TableColumn<>();
  private final TableColumn<LogData, ArrayList<LogData.StringPart>>
    _evaluatedColumn = new TableColumn<>();
  private final TableColumn<LogData, LogData.DatePart> _timeColumn
    = new TableColumn<>();


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  public void init(Project project, Editor editor,
    GlobalConfiguration globalConf, TableView tw) {
    _globalConf = globalConf;
    _editor = editor;
    _ruleLoggingTableView = tw;
    _vonda = project.vonda;
    _project = project;

    initRuleLoggingTableView();
    _vonda.logOutputProperty().addListener(incomingRuleLogListener);

    /* this listener updates the timeStampIndex setting in the tableView */
    globalConf.timeStampIndexProperty().addListener(cl -> {
        updateTimeStampIndexSetting();
    });
  }


  /** Adds new ruleLogging output to the ruleLoggingList. */
  private final ChangeListener<LogData> incomingRuleLogListener = ((o, ov, nv) -> {
    if (nv != null) {
      Platform.runLater(() -> {
        ruleLoggingList.add(0, nv);
        _ruleLoggingTableView.sort();
      });
    }
  });

  public void adaptTableViewColumns() {
    Double correctionValue = 18.0;
    Double prefWidth = _ruleLoggingTableView.widthProperty().getValue()
            - _labelColumn.getWidth() - _timeColumn.getWidth()
            - correctionValue;
    _evaluatedColumn.setPrefWidth(prefWidth);
  }

  public void updateTimeStampIndexSetting() {
    boolean timeStampIndexSetting = _globalConf.timeStampIndexProperty().get();
    _timeColumn.setCellFactory(value ->
          new TimestampCellFactory(timeStampIndexSetting)
      );
    if (timeStampIndexSetting) {
      _timeColumn.setPrefWidth(126.24658203125);
    } else {
      _timeColumn.setPrefWidth(105.3310546875);
    }
  }

  private void initLabelColumn(TableColumn<LogData, LogData.StringPart> tc) {
    tc.setText("Label");
    tc.setPrefWidth(180.0);
    tc.setCellValueFactory(v -> v.getValue().label);
    tc.setCellFactory(v -> new LabelCellFactory());
    tc.setComparator((x, y) -> x.content.compareTo(y.content));
    tc.widthProperty().addListener(v -> adaptTableViewColumns());
  }

  private void initEvaluatedColumn(
    TableColumn<LogData, ArrayList<LogData.StringPart>> tc) {

    tc.setText("Evaluated");
    tc.setCellValueFactory(v -> v.getValue().evaluatedRuleParts);
    tc.setCellFactory(v -> new EvaluatedCellFactory());
    tc.widthProperty().addListener(v -> adaptTableViewColumns());
  }

  private void initTimeColumn(TableColumn<LogData, LogData.DatePart> tc,
      boolean showTimestamp) {
    tc.setText("Time");
    tc.setCellValueFactory(v -> v.getValue().timestamp);
    tc.setCellFactory(v -> new TimestampCellFactory(showTimestamp));
    tc.setComparator((x, y) -> (
            dt.format(x.date) + Integer.toString(x.counter))
            .compareToIgnoreCase(
            dt.format(y.date) + Integer.toString(y.counter))
    );
    tc.widthProperty().addListener((cl, ov, nv) -> adaptTableViewColumns());
  }

  private void initRuleLoggingTableView() {
    initLabelColumn(_labelColumn);
    initEvaluatedColumn(_evaluatedColumn);
    initTimeColumn(_timeColumn, _globalConf.timeStampIndexProperty().get());
    updateTimeStampIndexSetting();

    _ruleLoggingTableView.getColumns().addAll(
      _timeColumn, _labelColumn, _evaluatedColumn);
    _ruleLoggingTableView.setItems(ruleLoggingList);

    /* set cell height of TableView */
    _ruleLoggingTableView.setFixedCellSize(25);

    /* set column resizing policies */
    _ruleLoggingTableView.widthProperty().addListener((cl, ov, nv) -> {
      adaptTableViewColumns();
    });

    /* Jump to selected rule. */
    _ruleLoggingTableView.getSelectionModel().selectedItemProperty()
      .addListener((o, ov, nv) -> openRule(((LogData) nv).getRuleId()));
  }

  private void openRule(int ruleId) {
    RuleInfoExtended rule = _project.getRuleModel().getRule(ruleId);
    _editor.loadFileAtLine(rule.getSourceFile(), rule.getLine());
  }

}

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

import de.dfki.mlt.rudimant.common.BasicInfo;
import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.FileTreeView.RudiTreeCell;
import de.dfki.mlt.rudibugger.FileTreeView.RudiPath;
import de.dfki.mlt.rudibugger.RuleTreeView.BasicInfoTreeCell;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller manages the left part of the rudibugger window: <br>
 *   - the TreeView showing <code>.rudi</code> files, <br>
 *   - the TreeView showing Imports and Rules, and <br>
 *   - some buttons that manipulate these <code>TreeView</code>s. <br>
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SideBarController extends Controller {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("sideBarController");


  /*****************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   ****************************************************************************/

  /**
   * Connects this controller to the DataModel and initializes it by defining
   * different listeners and cell factories.
   *
   * @param model
   *        The current DataModel
   */
  public void init(DataModel model) {
    linkModel(model);

    defineRudiTreeView();
    defineRuleTreeView();

    /* this Listener keeps the rudiTreeView containing the .rudi files up to date */
    model.projectLoadedProperty().addListener((o, ov, nv) -> {
      if (nv) {
        rudiTreeView.setRoot(model.rudiHierarchy.getRoot());
        rudiTreeView.setShowRoot(false);
      }
      else
        rudiTreeView.setRoot(null);

    });

    /* this Listener builds or modifies the RuleTreeView, if the RuleModel
    was changed.*/
    model.ruleModel.changedStateProperty().addListener((o, oldVal, newVal) -> {
      switch ((int) newVal) {
        case RULE_MODEL_NEWLY_CREATED:
          log.debug("RuleModel has been found.");
          ruleTreeView.setRoot(buildRuleTreeView(model.ruleModel.getRootImport()));
          ruleTreeView.getRoot().setExpanded(true);
          markFilesInRudiList();
          linkRudiPathsToImportInfos();
          model.ruleModel.setChangedStateProperty(RULE_MODEL_UNCHANGED);
          break;
        case RULE_MODEL_CHANGED:
          log.debug("RuleModel has been modified.");
          _model.ruleModelState.retrieveStateOf(ruleTreeView);
          ruleTreeView.setRoot(buildRuleTreeView(model.ruleModel.getRootImport()));
          _model.ruleModelState.setStateOf(ruleTreeView);
          markFilesInRudiList();
          linkRudiPathsToImportInfos();
          model.ruleModel.setChangedStateProperty(RULE_MODEL_UNCHANGED);
          break;
        case RULE_MODEL_REMOVED:
          ruleTreeView.setRoot(null);
          if (_model.rudiHierarchy.getRoot() != null) {
            markFilesInRudiList();
            linkRudiPathsToImportInfos();
          }
          log.debug("RuleModel has been resetted / removed");
          model.ruleModel.setChangedStateProperty(RULE_MODEL_UNCHANGED);
          break;
        default:
          break;
      }
    });

    /* Listen to request for saving ruleLoggingState */
    _model.ruleModelState.saveRequestProperty().addListener((o, ov, nv) -> {
      if (nv) {
        log.debug("Requested to save ruleLoggingState.");
        _model.ruleModelState.retrieveStateOf(ruleTreeView);
        _model.ruleModelState.saveState();
        _model.ruleModelState.resetSaveRequestProperty();
      }
    });

    /* Listen to request for loading ruleLoggingState */
    _model.ruleModelState.loadRequestProperty().addListener((o, ov, nv) -> {
      if (nv == null) return;
      _model.ruleModelState.setStateOf(ruleTreeView);
      _model.ruleModelState.resetLoadRequestProperty();
    });

  }

  /** Defines the look and feel of the rudiTreeView. */
  private void defineRudiTreeView() {
    rudiTreeView.setCellFactory(value -> new RudiTreeCell());
    rudiTreeView.setOnMouseClicked((MouseEvent mouseEvent) -> {
      if (mouseEvent.getClickCount() == 2) {
        TreeItem ti = (TreeItem) rudiTreeView.getSelectionModel()
          .getSelectedItem();
        RudiPath rp = (RudiPath) ti.getValue();
        if (! Files.isDirectory(rp.getPath()))
          _model.rudiLoad.openFile(rp.getPath());
      }
    });
  }

  /** Defines the look and feel of the ruleTreeView. */
  private void defineRuleTreeView() {
    ruleTreeView.setCellFactory(value -> new BasicInfoTreeCell());
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Marks the files in the <b>rudiList</b> according to their usage state. */
  private void markFilesInRudiList() {
    Path mainRudi = _model.ruleModel.getRootImport().getAbsolutePath();
    Path wrapperRudi = _model.project.getWrapperClass();

    _model.rudiHierarchy.getRudiPathSet().forEach((x) -> {
      if (mainRudi.getFileName().equals(x.getPath().getFileName()))
        x.usedProperty().setValue(FILE_IS_MAIN);
      else if (wrapperRudi.getFileName().equals(x.getPath().getFileName()))
        x.usedProperty().setValue(FILE_IS_WRAPPER);
      else if (_model.ruleModel.getImportSet().contains(x.getPath()))
        x.usedProperty().setValue(FILE_USED);
      else
        x.usedProperty().setValue(FILE_NOT_USED);
    });
  }

  /**
   * Links all the <code>ImportInfoExtended</code> from the
   * <code>RuleModel</code> to their respective <code>RudiPath</code>.
   */
  private void linkRudiPathsToImportInfos() {
    _model.rudiHierarchy.getRudiPathSet().forEach(x -> {
      if (_model.ruleModel.getImportSet().contains(x.getPath()))
        x.setImportInfo(_model.ruleModel.getPathToImportMap().get(x.getPath()));
      else x.setImportInfo(null);
    });
  }

  /** Expands a given TreeItem and all its children. */
  private static void expandTreeItem(TreeItem item) {
    item.setExpanded(true);
    item.getChildren().forEach(c ->
      expandTreeItem((TreeItem) c));
  }

  /** Collapses a given TreeItem and all its children. */
  private static void collapseTreeItem(TreeItem item) {
    item.setExpanded(false);
    item.getChildren().forEach(c ->
      collapseTreeItem((TreeItem) c));
  }

  /** Builds the ruleTreeView. */
  private static TreeItem buildRuleTreeView(BasicInfo bi) {
    TreeItem treeItem = new TreeItem(bi);
    bi.getChildren().forEach(o ->
      treeItem.getChildren().add(buildRuleTreeView(o)));
    return treeItem;
  }


  /*****************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS AND PROPERTIES
   ****************************************************************************/

  /** @return The SplitPane separating rule- and fileTreeView. */
  public SplitPane getSidebarSplitPane() { return sidebarSplitPane; }


  /*****************************************************************************
   * GUI ELEMENTS
   ****************************************************************************/

  /** Represents the root splitPane */
  @FXML
  private SplitPane sidebarSplitPane;

  /** Shows the different <code>.rudi</code> rules and imports. */
  @FXML
  private TreeView ruleTreeView;

  /** Shows the content of the <code>.rudi</code> folder. */
  @FXML
  private TreeView rudiTreeView;


  /*****************************************************************************
   * GUI ACTIONS
   ****************************************************************************/

  /** Expands all <code>TreeItem</code>s in the ruleTreeView. */
  @FXML
  private void expandAll(ActionEvent event) {
    if (ruleTreeView.getRoot() != null) expandTreeItem(ruleTreeView.getRoot());
  }

  /**
   * Collapses all <code>TreeItem</code>s, but the root item, in the
   * ruleTreeView.
   */
  @FXML
  private void collapseAll(ActionEvent event) {
    if (ruleTreeView.getRoot() != null) {
      collapseTreeItem((TreeItem) ruleTreeView.getRoot());
      ruleTreeView.getRoot().setExpanded(true);
    }
  }

}

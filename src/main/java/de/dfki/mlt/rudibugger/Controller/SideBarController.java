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
import de.dfki.mlt.rudibugger.FileTreeView.RudiHierarchy;
import de.dfki.mlt.rudibugger.FileTreeView.RudiTreeCell;
import de.dfki.mlt.rudibugger.Project.Project;
import de.dfki.mlt.rudibugger.Project.RuleModel.RuleModel;
import de.dfki.mlt.rudibugger.Project.RuleModel.State.RuleModelState;
import de.dfki.mlt.rudibugger.RuleTreeView.BasicInfoTreeCell;
import java.nio.file.Path;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
public class SideBarController {

  static Logger log = LoggerFactory.getLogger("sideBarController");

  /** TODO */
  private DataModel _model;


  /* ***************************************************************************
   * GUI ELEMENTS
   * **************************************************************************/

  /** Represents the root splitPane. */
  @FXML
  private SplitPane sidebarSplitPane;

  /** Shows the different <code>.rudi</code> rules and imports. */
  @FXML
  private TreeView ruleTreeView;

  /** Shows the content of the <code>.rudi</code> folder. */
  @FXML
  private TreeView rudiTreeView;


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
    _model = model;



    /* this Listener keeps the rudiTreeView containing the .rudi files up to date */
    _model.isProjectLoadedProperty().addListener((o, ov, nv) -> {

      if (nv) {
        rudiTreeView.setCellFactory(value
            -> new RudiTreeCell(_model.getCurrentProject()));
        ruleTreeView.setCellFactory(value
            -> new BasicInfoTreeCell(_model.getCurrentProject(), _model.globalConf));
        rudiTreeView.setRoot(_model.getCurrentProject().getRudiHierarchy().getRoot());
        rudiTreeView.setShowRoot(false);
        _model.getCurrentProject().getRuleModel().changedStateProperty()
                .addListener(ruleModelListener);
      } else {
        rudiTreeView.setRoot(null);
        ruleTreeView.setRoot(null);
        _model.getCurrentProject().getRuleModel().changedStateProperty()
              .removeListener(ruleModelListener);
      }


      /* Listen to request for saving ruleLoggingState */
      _model.getCurrentProject().getRuleModel().getRuleModelState()
              .saveRequestProperty().addListener((o1, ov1, nv1) -> {
        if (nv1 != null) {
          log.debug("Requested to save ruleLoggingState.");
          RuleModelState rms = _model.getCurrentProject().getRuleModel()
                  .getRuleModelState();
          rms.retrieveStateOf(ruleTreeView);
          rms.saveState(nv1);
          rms.resetSaveRequestProperty();
        }
      });
    });
    
    /* Listen to request for loading ruleLoggingState */
//    _model.ruleModelState.loadRequestProperty().addListener((o, ov, nv) -> {
//      if (nv == null) return;
//      _model.ruleModelState.setStateOf(ruleTreeView);
//      _model.ruleModelState.resetLoadRequestProperty();
//    });

  }



  /** Builds or modifies the RuleTreeView, if the RuleModel was changed. */
  private final ChangeListener ruleModelListener = (o, oldVal, newVal) -> {
    Project project = _model.getCurrentProject();
    switch ((int) newVal) {
      case RULE_MODEL_NEWLY_CREATED:
        log.debug("RuleModel has been found.");
        ruleTreeView.setRoot(buildRuleTreeView(project.getRuleModel().getRootImport()));
        ruleTreeView.getRoot().setExpanded(true);
        markFilesInRudiList();
        linkRudiPathsToImportInfos();
        break;
      case RULE_MODEL_CHANGED:
        log.debug("RuleModel has been modified.");
        project.getRuleModel().getRuleModelState().retrieveStateOf(ruleTreeView);
        ruleTreeView.setRoot(buildRuleTreeView(project.getRuleModel().getRootImport()));
        project.getRuleModel().getRuleModelState().setStateOf(ruleTreeView);
        markFilesInRudiList();
        linkRudiPathsToImportInfos();
        break;
      case RULE_MODEL_REMOVED:
        ruleTreeView.setRoot(null);
        if (project.getRudiHierarchy().getRoot() != null) {
          markFilesInRudiList();
          linkRudiPathsToImportInfos();
        }
        log.debug("RuleModel has been resetted / removed");
        break;
      default:
        break;
    }
    project.getRuleModel().setChangedStateProperty(RULE_MODEL_UNCHANGED);

  };



  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /** Marks the files in the <b>rudiList</b> according to their usage state. */
  private void markFilesInRudiList() {
    Project project = _model.getCurrentProject();
    RudiHierarchy rudiHierarchy = project.getRudiHierarchy();
    RuleModel ruleModel = project.getRuleModel();
    Path wrapperRudi = project.getWrapperClass();
    Path mainRudi = project.getRuleModel().getRootImport().getAbsolutePath();

    rudiHierarchy.getRudiPathSet().forEach((x) -> {
      if (mainRudi.getFileName().equals(x.getPath().getFileName()))
        x.usedProperty().setValue(FILE_IS_MAIN);
      else if (wrapperRudi.getFileName().equals(x.getPath().getFileName()))
        x.usedProperty().setValue(FILE_IS_WRAPPER);
      else if (ruleModel.getImportSet().contains(x.getPath()))
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
    Project project = _model.getCurrentProject();
    RudiHierarchy rudiHierarchy = project.getRudiHierarchy();
    RuleModel ruleModel = project.getRuleModel();

    rudiHierarchy.getRudiPathSet().forEach(x -> {
      if (ruleModel.getImportSet().contains(x.getPath()))
        x.setImportInfo(ruleModel.getPathToImportMap().get(x.getPath()));
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



  /* ***************************************************************************
   * GUI ACTIONS
   * **************************************************************************/

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

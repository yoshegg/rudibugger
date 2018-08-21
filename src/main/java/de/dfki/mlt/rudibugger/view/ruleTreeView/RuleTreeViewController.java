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
package de.dfki.mlt.rudibugger.view.ruleTreeView;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudimant.common.BasicInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleTreeViewController {

  static Logger log = LoggerFactory.getLogger("ruleTreeViewCon.");
  private DataModel _model;


  /* ***************************************************************************
   * GUI ELEMENTS
   * **************************************************************************/

  /** Shows the different <code>.rudi</code> rules and imports. */
  @FXML
  private TreeView ruleTreeView;


  /* ***************************************************************************
   * INITIALIZERS
   * **************************************************************************/

  /**
   * Connects this controller to the DataModel and initializes it by defining
   * different listeners and cell factories.
   *
   * @param model
   *        The current DataModel
   */
  public void init(DataModel model) {
    _model = model;
    listenForLoadedProject();
  }

  private void listenForLoadedProject() {
    _model.loadedProjectProperty().addListener((o, ov, project) -> {
      if (project != null) {
        ruleTreeView.setCellFactory(value
                -> new BasicInfoTreeCell(_model.getEditor(), _model.globalConf));
        if (project.getRuleModel() != null) {
          ruleTreeView.setRoot(buildRuleTreeView(
                  project.getRuleModel().getRootImport()));
          ruleTreeView.getRoot().setExpanded(true);
        }
        listenForRuleModel(project);
      } else {
        ruleTreeView.setRoot(null);
        ruleTreeView.setCellFactory(null);
      }
    });
  }

  private void listenForRuleModel(Project project) {
    project.ruleModelProperty().addListener((o, oldRuleModel, newRuleModel) -> {
      if (newRuleModel != null) {
        log.debug("RuleModel created / updated, updating ruleTreeView...");
        RuleTreeViewState oldRms = RuleTreeViewState.retrieveStateOf(ruleTreeView);
        ruleTreeView.setRoot(buildRuleTreeView(newRuleModel.getRootImport()));
        RuleTreeViewState.setStateOf(oldRms, ruleTreeView);
      } else {
        ruleTreeView.setRoot(null);
        log.debug("ruleTreeView has been resetted, RuleModel has been removed.");
      }
    });
  }


  /* ***************************************************************************
   * GETTERS
   * **************************************************************************/

  public TreeView getTreeView() { return ruleTreeView; }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /** Builds the ruleTreeView. */
  private static TreeItem buildRuleTreeView(BasicInfo bi) {
    TreeItem treeItem = new TreeItem(bi);
    bi.getChildren().forEach(o ->
      treeItem.getChildren().add(buildRuleTreeView(o)));
    return treeItem;
  }

  /** Expands a given TreeItem and all its children. */
  private static void expandTreeItems(TreeItem item) {
    item.setExpanded(true);
    item.getChildren().forEach(c -> expandTreeItems((TreeItem) c));
  }

  /** Collapses a given TreeItem and all its children. */
  private static void collapseTreeItems(TreeItem item) {
    item.setExpanded(false);
    item.getChildren().forEach(c -> collapseTreeItems((TreeItem) c));
  }


  /* ***************************************************************************
   * GUI ACTIONS
   * **************************************************************************/

  /** Expands all <code>TreeItem</code>s in the ruleTreeView. */
  @FXML
  private void expandAll(ActionEvent event) {
    if (ruleTreeView.getRoot() != null) expandTreeItems(ruleTreeView.getRoot());
  }

  /**
   * Collapses all <code>TreeItem</code>s, but the root item, in the
   * ruleTreeView.
   */
  @FXML
  private void collapseAll(ActionEvent event) {
    if (ruleTreeView.getRoot() != null) {
      collapseTreeItems((TreeItem) ruleTreeView.getRoot());
      ruleTreeView.getRoot().setExpanded(true);
    }
  }

}

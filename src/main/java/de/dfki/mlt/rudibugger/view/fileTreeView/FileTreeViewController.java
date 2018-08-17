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
package de.dfki.mlt.rudibugger.view.fileTreeView;

import static de.dfki.mlt.rudibugger.Constants.FILE_IS_MAIN;
import static de.dfki.mlt.rudibugger.Constants.FILE_IS_WRAPPER;
import static de.dfki.mlt.rudibugger.Constants.FILE_NOT_USED;
import static de.dfki.mlt.rudibugger.Constants.FILE_USED;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.view.fileTreeView.RudiHierarchy;
import de.dfki.mlt.rudibugger.view.fileTreeView.RudiTreeCell;
import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudibugger.project.ruleModel.RuleModel;
import java.nio.file.Path;
import javafx.fxml.FXML;
import javafx.scene.control.TreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class FileTreeViewController {

  static Logger log = LoggerFactory.getLogger("fileTreeViewCon.");
  private DataModel _model;


  /* ***************************************************************************
   * GUI ELEMENTS
   * **************************************************************************/

  /** Shows the content of the <code>.rudi</code> folder. */
  @FXML
  private TreeView fileTreeView;


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
        fileTreeView.setCellFactory(value -> new RudiTreeCell(project));
        fileTreeView.setRoot(project.getRudiHierarchy().getRoot());
        fileTreeView.setShowRoot(false);
        markFilesInRudiList(project.getRudiHierarchy(), project.getRuleModel(),
                project.getWrapperClass());
        linkRudiPathsToImportInfos(project.getRudiHierarchy(),
                project.getRuleModel());
        listenForRuleModel(project);
      } else {
        fileTreeView.setCellFactory(null);
        fileTreeView.setRoot(null);
      }
    });
  }

  private void listenForRuleModel(Project project) {
    project.ruleModelProperty().addListener((o, oldRuleModel, newRuleModel) -> {
      if (newRuleModel != null) {
        log.debug("RuleModel created / updated, updating fileTreeView...");
        markFilesInRudiList(project.getRudiHierarchy(), newRuleModel,
                project.getWrapperClass());
        linkRudiPathsToImportInfos(project.getRudiHierarchy(),
                project.getRuleModel());
      } else {
        fileTreeView.setRoot(null);
        log.debug("fileTreeView's icons have been resetted because RuleModel "
                + "has been removed.");
      }
    });
  }

  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /** Marks the files in the <b>rudiList</b> according to their usage state. */
  private void markFilesInRudiList(RudiHierarchy rudiHierarchy,
          RuleModel ruleModel, Path wrapperRudi) {
    Path mainRudi = ruleModel.getRootImport().getAbsolutePath();

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
  private void linkRudiPathsToImportInfos(RudiHierarchy rudiHierarchy,
          RuleModel ruleModel) {
    rudiHierarchy.getRudiPathSet().forEach(x -> {
      if (ruleModel.getImportSet().contains(x.getPath()))
        x.setImportInfo(ruleModel.getPathToImportMap().get(x.getPath()));
      else x.setImportInfo(null);
    });
  }
}

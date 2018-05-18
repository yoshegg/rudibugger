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

package de.dfki.mlt.rudibugger.RuleTreeView;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.HelperWindows;
import de.dfki.mlt.rudibugger.RuleModel.ImportInfoExtended;
import de.dfki.mlt.rudibugger.RuleModel.RuleInfoExtended;
import de.dfki.mlt.rudimant.common.BasicInfo;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Represents a complete RuleModel's state. It includes
 *  - the ruleLoggingState of every known rule,
 *  - the expansion state of every item in the ruleTreeView, and
 *  - the scrollbar position in the ruleTreeView. //TODO
 *
 * It can be loaded from another file or saved for further use.
 *
 * Furthermore, it never forgets, meaning that a rule that ever appeared under
 * certain circumstances will be remembered forever. This is useful if a certain
 * Import has not been used for a while but the selection of its rules should
 * be used in the future. Using that Import again will then know its last Rules'
 * ruleLoggingState.
 *
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleModelState {

  /** The Logger. */
  static Logger log = LoggerFactory.getLogger("RuleModel");

  /** The <code>DataModel</code>. */
  private DataModel _model;

  /** Root item of RuleModelState structure. */
  private RuleStateItem _root;

  /** Signalizes a save request of a ruleLoggingState. */
  private final SimpleBooleanProperty _saveRequestProperty
    = new SimpleBooleanProperty(false);

  /** Signalizes a load request of a ruleLoggingState. */
  private final SimpleBooleanProperty _loadRequestProperty
    = new SimpleBooleanProperty(false);

  /** Represents a list of all recent <code>RuleModelState</code>s. */
  private List<Path> _recentStates = new ArrayList<>();


  /*****************************************************************************
   * INITIALIZER
   ****************************************************************************/

  /**
   * Initializes this project specific addition of <code>DataModel</code>.
   *
   * @param model The current <code>DataModel</code>
   */
  public RuleModelState(DataModel model) {
    _model = model;
  }

  /** Creates a new RuleModelState. (<i>Constructor needed for YAML</i>) */
  private RuleModelState() {}

  /** Sets the DataModel of RuleModelState. (<i>Function needed for YAML</i>) */
  private void setDataModel(DataModel model) { _model = model; }


  /*****************************************************************************
   * RETRIEVE AND SET STATE METHODS
   ****************************************************************************/

  /**
   * Retrieves the state of the ruleTreeView.
   *
   * @param tw
   *        The current ruleTreeView
   */
  public void retrieveStateOf(TreeView tw) {

    /* get root TreeItem of TreeView */
    TreeItem<ImportInfoExtended> root = tw.getRoot();

    /* the root has not been created yet or the name does not match */
    if (_root == null
      || !_root.getLabel().equals(root.getValue().getLabel())) {
      _root = new RuleStateItem(root.getValue().getLabel(),
        root.isExpanded(), root.getValue().getState());
      _root.isImport(true);
    } else {
      _root.updateRuleStateItem(
        root.isExpanded(), root.getValue().getState()
      );
    }

    /* create the children and add them */
    _root.addChildren(retrieveStateOfHelper(root, _root));

  }

  /** Helper function of <code>retrieveStateOf</code>. */
  private HashMap<String, RuleStateItem>
          retrieveStateOfHelper(TreeItem tempItem, RuleStateItem ruleItem) {

    /* the returned RuleTreeViewStateItems */
    HashMap<String, RuleStateItem> map = new HashMap<>();

    /* iterate over the children */
    for (Object child : tempItem.getChildren()) {
      RuleStateItem ruleStateItem;

      TreeItem<BasicInfo> item = (TreeItem) child;

      if (((TreeItem) child).getValue() instanceof RuleInfoExtended) {
        RuleInfoExtended itemValue
                = (RuleInfoExtended) ((TreeItem) child).getValue();

        /* is the child already known? if not: create a new one */
        if (ruleItem.getChildrenNames().contains(item.getValue().getLabel())) {
          ruleStateItem = ruleItem.getChild(item.getValue().getLabel());
          ruleStateItem.updateRuleStateItem(
                  item.isExpanded(), itemValue.getState()
          );
        } else {
          ruleStateItem = new RuleStateItem(item.getValue().getLabel(),
                  item.isExpanded(), itemValue.getState());

          /* if it is an import, mark it */
          if (item.getValue() instanceof ImportInfoExtended) {
            ruleStateItem.isImport(true);
          }
        }
      }

      else {
        ImportInfoExtended itemValue
                = (ImportInfoExtended) ((TreeItem) child).getValue();

        /* is the child already known? if not: create a new one */
        if (ruleItem.getChildrenNames().contains(item.getValue().getLabel())) {
          ruleStateItem = ruleItem.getChild(item.getValue().getLabel());
          ruleStateItem.updateRuleStateItem(
                  item.isExpanded(), itemValue.getState()
          );
        } else {
          ruleStateItem = new RuleStateItem(item.getValue().getLabel(),
                  item.isExpanded(), itemValue.getState());

          /* if it is an import, mark it */
          if (item.getValue() instanceof ImportInfoExtended) {
            ruleStateItem.isImport(true);
          }
        }
      }

      /* create the children and add them */
      ruleStateItem.addChildren(retrieveStateOfHelper(item, ruleStateItem));

      /* add them to the returned set */
      map.put(item.getValue().getLabel(), ruleStateItem);
    }
    return map;
  }

  /**
   * Sets the state of a given ruleTreeView.
   *
   * @param tw
   *        The current ruleTreeView
   */
  public void setStateOf(TreeView tw) {

    /* get root TreeItem of TreeView */
    TreeItem<ImportInfoExtended> root = (TreeItem) tw.getRoot();

    /* has this item already appeared once? */
    if (root.getValue().getLabel().equals(_root.getLabel())) {

      /* set the expansion state */
      root.setExpanded(_root.getProps().getIsExpanded());

      /* iterate over the children */
      for (Object x : root.getChildren()) {
        TreeItem y = (TreeItem) x;
        setStateOfHelper(y, _root);
      }
    }

  }

  /** Helper function of <code>setStateOf()</code>. */
  private void setStateOfHelper(TreeItem<BasicInfo> obj, RuleStateItem item) {

    String lab = obj.getValue().getLabel();

    /* has this TreeItem already appeared once? */
    if (item.getChildrenNames().contains(lab)) {

      /* set the expansion state */
      obj.setExpanded(item.getChild(lab).getProps().getIsExpanded());

      /* if this is a rule, also set the log state */
      if (obj.getValue() instanceof RuleInfoExtended) {
        RuleInfoExtended rule = (RuleInfoExtended) obj.getValue();
        rule.setState(item.getChild(lab).getProps().getLoggingState());
      }

      /* iterate over the children */
      for (Object x : obj.getChildren()) {
        TreeItem<BasicInfo> y = (TreeItem) x;
        setStateOfHelper(y, item.getChild(lab));
      }
    }
  }


  /*****************************************************************************
   * SAVE AND LOAD METHODS
   ****************************************************************************/

  /** Saves the current RuleModel state in a file. */
  public void saveState() {
    Path savePath = _model.project.getRuleModelStatesFolder();
    if (! Files.exists(savePath)) savePath.toFile().mkdirs();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(savePath.toFile());
    FileChooser.ExtensionFilter extFilter
            = new FileChooser.ExtensionFilter
        ("YAML file (*" + "yml" + ")", "*" + "yml");
    fileChooser.getExtensionFilters().add(extFilter);
    Path file;
    try {
      file = (fileChooser.showSaveDialog(_model.stageX)).toPath();
    } catch (NullPointerException e) {
      return;
    }

    if (!file.getFileName().toString().endsWith(".yml")) {
      file = Paths.get(file.toString() + ".yml");
    }

    try {
      FileWriter writer = new FileWriter(savePath.resolve(file).toFile());
      _model.yaml.dump(this, writer);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
    log.debug("Saved file " + file.toString());

  }

  /** Loads a saved RuleModel state from a file. */
  public void loadState(Path path) {
    RuleModelState rtvs;
    try {
      Yaml yaml = new Yaml();
      rtvs = (RuleModelState) yaml.load(new FileReader(path.toFile()));
    } catch (FileNotFoundException e) {
      log.error("Could not read in configuration file");
      return;
    }

    rtvs.setDataModel(_model);
    _root = rtvs.getRoot();
    loadRequestProperty().set(true);
  }

  /** Opens a FileSelection window to choose a RuleModelState file. */
  public void loadStateSelectFile() {
    Path chosenFile = HelperWindows.openRuleLoggingStateFile(
      _model.stageX, _model.project.getRuleModelStatesFolder());
    if (chosenFile == null) return;
    loadState(chosenFile);
  }

  /** Resets the loadRequestProperty. */
  public void resetLoadRequestProperty() { _loadRequestProperty.set(false); }

  /** Resets the saveRequestProperty. */
  public void resetSaveRequestProperty() { _saveRequestProperty.set(false); }

  /** Signalizes a save request. */
  public void requestSave() { _saveRequestProperty.set(true); }


  /*****************************************************************************
   * OTHER METHODS
   ****************************************************************************/

  /**
   * Retrieves the 10 most recent saved RuleModelState configurations.
   */
  public void retrieveRecentConfigurations() {
    List<Path> temp = new ArrayList<>();

    /* Retrieve all files and add them to a list. */
    Stream<Path> stream;
    try {
      stream = Files.walk(_model.project.getRuleModelStatesFolder());
    } catch (IOException e) {
      log.error(e.toString());
      return;
    }
    stream.forEach(x -> { if (!Files.isDirectory(x)) temp.add(x); });

    /* Sort the list by modification date. */
    Collections.sort(temp, (Path p1, Path p2) ->
        Long.compare(p2.toFile().lastModified(), p1.toFile().lastModified()));

    /* Set the last 10 as the one's that will be shown in the menu. */
    int subListLength = 10;
    if (temp.size() < 10) subListLength = temp.size();
    _recentStates = temp.subList(0, subListLength);
  }


  /*****************************************************************************
   * PRETTY PRINTING
   ****************************************************************************/

  /**
   * Pretty prints the RuleModelState.
   *
   * @return Pretty String
   */
  @Override
  public String toString() {
    if (_root == null) {
      return "RuleTreeViewState is empty";
    }

    String returnVal = "";
    returnVal += _root.toString() + "\n";

    String prefix = "  ";
    returnVal += toStringHelper(_root, prefix);

    return returnVal;
  }

  /** Helper function of <code>toString()</code>. */
  private String toStringHelper(RuleStateItem e, String prefix) {
    String returnVal = "";
    for (RuleStateItem x : e.getChildrenValues()) {
      returnVal += prefix + x.toString() + "\n";
      prefix += "  ";
      returnVal += toStringHelper(x, prefix);
      prefix = prefix.substring(0, prefix.length() - 2);
    }
    return returnVal;
  }


  /*****************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS AND PROPERTIES
   ****************************************************************************/

  /** @return A list of all recently saved <code>RuleModelState</code>s */
  public List<Path> getRecentStates() {
    retrieveRecentConfigurations();
    return _recentStates;
  }

  /** @return Signalizes a save request of the RuleModelState. */
  public BooleanProperty saveRequestProperty() { return _saveRequestProperty; }

  /** @return Signalizes a load request of a RuleModelState. */
  public BooleanProperty loadRequestProperty() { return _loadRequestProperty; }

  /** @Return The root item of RuleModelState */
  public RuleStateItem getRoot() { return _root; }

  /** Sets the root item of RuleModelState. */
  public void setRoot(RuleStateItem root) { _root = root; }

}

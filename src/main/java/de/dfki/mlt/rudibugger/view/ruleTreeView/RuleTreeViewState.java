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

import de.dfki.mlt.rudibugger.project.ruleModel.ImportInfoExtended;
import de.dfki.mlt.rudibugger.project.ruleModel.RuleInfoExtended;
import de.dfki.mlt.rudimant.common.BasicInfo;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Represents a complete RuleModel's state. It includes
 *  - the logging state of every known rule,
 *  - the expansion state of every item in the ruleTreeView, and
 *  - the scrollbar position in the ruleTreeView. //TODO
 *
 * It can be loaded from another file or saved for further use.
 *
 * Furthermore, it never forgets, meaning that a rule that ever appeared under
 * certain circumstances will be remembered forever. This is useful if a certain
 * Import has not been used for a while but the selection of its rules should be
 * used in the future. Using that Import again will also re-enable its
 * selections.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleTreeViewState {

  static Logger log = LoggerFactory.getLogger("RuleModel");

  private static final Yaml YAML = new Yaml(
    new DumperOptions() {{
      setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }}
  );


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** Root item of RuleTreeViewState structure. */
  private RuleTreeViewStateItem _root;


  /* ***************************************************************************
   * INITIALIZER / CONSTRUCTOR
   * **************************************************************************/

  /** Creates a new RuleModelState. */
  private RuleTreeViewState() {}


  /* ***************************************************************************
   * RETRIEVE AND SET STATE METHODS
   * **************************************************************************/

  /** Retrieves the state of a given TreeView. */
  public static RuleTreeViewState retrieveStateOf(TreeView tw) {

    RuleTreeViewState rms = new RuleTreeViewState();

    /* Get root TreeItem of given TreeView */
    TreeItem<ImportInfoExtended> root = tw.getRoot();

    rms._root = new RuleTreeViewStateItem(root.getValue().getLabel(),
            root.isExpanded(), root.getValue().getState());
    rms._root.isImport(true);

    /* Create children and add them */
    rms._root.addChildren(retrieveStateOfHelper(root, rms._root));

    return rms;

  }

  /** Helper function of <code>retrieveStateOf</code>. */
  private static HashMap<String, RuleTreeViewStateItem>
          retrieveStateOfHelper(TreeItem tempItem,
            RuleTreeViewStateItem ruleItem) {

    /* The returned RuleTreeViewStateItems */
    HashMap<String, RuleTreeViewStateItem> map = new HashMap<>();

    /* Iterate over the children */
    for (Object child : tempItem.getChildren()) {
      RuleTreeViewStateItem ruleStateItem;

      TreeItem<BasicInfo> item = (TreeItem) child;

      if (((TreeItem) child).getValue() instanceof RuleInfoExtended) {
        RuleInfoExtended itemValue
                = (RuleInfoExtended) ((TreeItem) child).getValue();

        /* Is the child already known? if not: create a new one */
        if (ruleItem.getChildrenNames().contains(item.getValue().getLabel())) {
          ruleStateItem = ruleItem.getChild(item.getValue().getLabel());
          ruleStateItem.setState(
                  item.isExpanded(), itemValue.getState()
          );
        } else {
          ruleStateItem = new RuleTreeViewStateItem(item.getValue().getLabel(),
                  item.isExpanded(), itemValue.getState());

          /* If it is an import, mark it */
          if (item.getValue() instanceof ImportInfoExtended) {
            ruleStateItem.isImport(true);
          }
        }
      }

      else {
        ImportInfoExtended itemValue
                = (ImportInfoExtended) ((TreeItem) child).getValue();

        /* Is the child already known? if not: create a new one */
        if (ruleItem.getChildrenNames().contains(item.getValue().getLabel())) {
          ruleStateItem = ruleItem.getChild(item.getValue().getLabel());
          ruleStateItem.setState(
                  item.isExpanded(), itemValue.getState()
          );
        } else {
          ruleStateItem = new RuleTreeViewStateItem(item.getValue().getLabel(),
                  item.isExpanded(), itemValue.getState());

          /* If it is an import, mark it */
          if (item.getValue() instanceof ImportInfoExtended) {
            ruleStateItem.isImport(true);
          }
        }
      }

      /* Create children and add them */
      ruleStateItem.addChildren(retrieveStateOfHelper(item, ruleStateItem));

      /* Add them to the returned set */
      map.put(item.getValue().getLabel(), ruleStateItem);
    }
    return map;
  }

  /** Applies a given state to a given TreeView */
  public static void setStateOf(RuleTreeViewState rms, TreeView tw) {

    /* Get root TreeItem of TreeView */
    TreeItem<ImportInfoExtended> root = (TreeItem) tw.getRoot();

    /* Has this item already appeared once? */
    if (root.getValue().getLabel().equals(rms.getRoot().getLabel())) {

      /* Set the expansion state */
      root.setExpanded(rms.getRoot().getProps().getIsExpanded());

      /* Iterate over the children */
      for (Object x : root.getChildren()) {
        TreeItem y = (TreeItem) x;
        setStateOfHelper(y, rms.getRoot());
      }
    }

  }

  /** Helper function of <code>setStateOf()</code>. */
  private static void setStateOfHelper(TreeItem<BasicInfo> obj,
          RuleTreeViewStateItem item) {

    String lab = obj.getValue().getLabel();

    /* Has this TreeItem already appeared once? */
    if (item.getChildrenNames().contains(lab)) {

      /* Set the expansion state */
      obj.setExpanded(item.getChild(lab).getProps().getIsExpanded());

      /* If this is a rule, also set the log state */
      if (obj.getValue() instanceof RuleInfoExtended) {
        RuleInfoExtended rule = (RuleInfoExtended) obj.getValue();
        rule.setState(item.getChild(lab).getProps().getLoggingState());
      }

      /* Iterate over the children */
      for (Object x : obj.getChildren()) {
        TreeItem<BasicInfo> y = (TreeItem) x;
        setStateOfHelper(y, item.getChild(lab));
      }
    }
  }


  /* ***************************************************************************
   * SAVE AND LOAD METHODS
   * **************************************************************************/

  /**
   * Saves the state of a given TreeView to a specified path.
   *
   * @param newFile The chosen save path
   * @param treeView The ruleTreeView
   */
  public static void saveState(Path newFile, TreeView treeView) {
    RuleTreeViewState rtvs = retrieveStateOf(treeView);
    try {
      FileWriter writer = new FileWriter(newFile.toFile());
      YAML.dump(rtvs, writer);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
    log.debug("Saved file " + newFile.toString());
  }

  /** Loads a saved RuleModel state from a file.
   *
   * @param path The chosen configuration's file
   * @param treeView The ruleTreeView
   */
  public static void loadState(Path path, TreeView treeView) {
    RuleTreeViewState rtvs;
    try {
      Yaml yaml = new Yaml();
      rtvs = (RuleTreeViewState) yaml.load(new FileReader(path.toFile()));
    } catch (FileNotFoundException e) {
      log.error("Could not read in configuration file");
      return;
    }
    setStateOf(rtvs, treeView);
  }


  /* ***************************************************************************
   * PRETTY PRINTING
   * **************************************************************************/

  /**
   * Pretty prints the RuleTreeViewState.
   *
   * @return Pretty String
   */
  @Override
  public String toString() {
    if (_root == null) return "RuleTreeViewState is empty";

    String returnVal = "";
    returnVal += _root.toString() + "\n";

    String prefix = "  ";
    returnVal += toStringHelper(_root, prefix);

    return returnVal;
  }

  /** Helper function of <code>toString()</code>. */
  private String toStringHelper(RuleTreeViewStateItem e, String prefix) {
    String returnVal = "";
    for (RuleTreeViewStateItem x : e.getChildrenValues()) {
      returnVal += prefix + x.toString() + "\n";
      prefix += "  ";
      returnVal += toStringHelper(x, prefix);
      prefix = prefix.substring(0, prefix.length() - 2);
    }
    return returnVal;
  }


  /* ***************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS
   * **************************************************************************/

  /** @return The root item of RuleModelState */
  public RuleTreeViewStateItem getRoot() { return _root; }

  /** Sets the root item of RuleTreeViewState. (NEEDED FOR YAML) */
  public void setRoot(RuleTreeViewStateItem root) { _root = root; }

}

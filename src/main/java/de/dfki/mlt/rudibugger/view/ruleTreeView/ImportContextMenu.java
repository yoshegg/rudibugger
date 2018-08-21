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

import de.dfki.mlt.rudibugger.GlobalConfiguration;
import de.dfki.mlt.rudibugger.editor.Editor;
import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudibugger.project.ruleModel.ImportInfoExtended;
import static de.dfki.mlt.rudimant.common.Constants.*;
import de.dfki.mlt.rudimant.common.ErrorInfo;
import java.util.LinkedHashMap;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;


/**
 * This is the context menu appearing when making a right click on an import in
 * the ruleTreeView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ImportContextMenu extends ContextMenu {

  /* ***************************************************************************
   * CONSTANTS
   * **************************************************************************/

  /**
   * Contains all the <code>RadioMenuItem</code>s of the
   * <code>ImportContextMenu</code>.
   */
  private static final LinkedHashMap<Integer, RadioMenuItem> RADIO_MENU_ITEMS
          = new LinkedHashMap<Integer, RadioMenuItem>() {{
      put(STATE_ALWAYS, new RadioMenuItem("Always log all child rules"));
      put(STATE_IF_TRUE, new RadioMenuItem("Log all child rules if true"));
      put(STATE_IF_FALSE, new RadioMenuItem("Log all child rules if false"));
      put(STATE_NEVER, new RadioMenuItem("Never log any child rules"));
    }};

  /**
   * Used to have a fictional member in the <code>ToggleGroup</code> so that
   * this one is selected when that ruleLogging state of the import is not
   * unique.
   */
  private static final RadioMenuItem PSEUDO_BUTTON = new RadioMenuItem();


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** The clicked Import. */
  private final ImportInfoExtended _item;

  private final Editor _editor;

  private final GlobalConfiguration _globalConf;


  /* ***************************************************************************
   * CONSTRUCTOR
   * **************************************************************************/

  /**
   * An <code>ImportContextMenu</code> should appear when a context menu was
   * requested by clicking on an import.
   */
  public ImportContextMenu(ImportInfoExtended ii, Editor editor,
          GlobalConfiguration globalConf) {
    super();
    _item = ii;
    _editor = editor;
    _globalConf = globalConf;
    initializeMenuItems();
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /** Initializes MenuItems. */
  private void initializeMenuItems() {
    initOpenMenuItem();
    if (!_item.getErrors().isEmpty())
      initErrorInfoItems();
    if (_item.containsRules()) {
      initRuleLoggingMenuItems();
      markCurrentRuleLoggingState();
    }
  }

  /** Creates the "Open" MenuItem in the context menu. */
  private void initOpenMenuItem() {
    CustomMenuItem openFile = new CustomMenuItem(new Label("Open "
            + _item.getAbsolutePath().getFileName().toString()));
    openFile.setOnAction((ActionEvent e) ->
      _editor.loadFile(_item.getAbsolutePath()));
    this.getItems().add(openFile);
  }

  /** Creates the "Go to error" MenuItem(s) in the context menu. */
  private void initErrorInfoItems() {
    addSeparator();
    if (_globalConf.showErrorInfoInRuleTreeViewContextMenu())
      _item.getErrors().forEach((e) -> treatErrorInfo(e));
  }

  /** Extracts ErrorInfo's information and adds it as MenuItem. */
  private void treatErrorInfo(ErrorInfo e) {
    String msg = e.getType().toString() + ": "
        + (e.getLocation().getBegin().getLine() + 1) + ":"
        + e.getLocation().getBegin().getColumn() + ": " + e.getMessage();
    Label label = new Label(msg);
    CustomMenuItem errorItem = new CustomMenuItem(label);
    Tooltip t = new Tooltip(e.getMessage());
    Tooltip.install(label, t);
    errorItem.setOnAction(f -> {
      _editor.loadFileAtLine(_item.getAbsolutePath(),
          e.getLocation().getBegin().getLine() + 1);
    });
    this.getItems().add(errorItem);
  }

  /** Creates the MenuItems defining how to log the Import's rules. */
  private void initRuleLoggingMenuItems() {
    addSeparator();
    ToggleGroup toggleGroup = new ToggleGroup();
    for (Integer s : RADIO_MENU_ITEMS.keySet()) {
      RADIO_MENU_ITEMS.get(s).setOnAction(e ->
        _item.setAllChildrenStates(s));
      this.getItems().add(RADIO_MENU_ITEMS.get(s));
      RADIO_MENU_ITEMS.get(s).setToggleGroup(toggleGroup);
    }
    /* Pseudo button because a toggle group should have something selected. */
    PSEUDO_BUTTON.setToggleGroup(toggleGroup);
  }

  /** Marks the corresponding MenuItem reflecting the ruleLoggingState. */
  private void markCurrentRuleLoggingState() {
    if (_item.getState() == STATE_PARTLY)
      PSEUDO_BUTTON.setSelected(true);
    else
      RADIO_MENU_ITEMS.get(_item.getState()).setSelected(true);
  }

  /** Adds a Separator MenuItem to the context menu. */
  private void addSeparator() {
    this.getItems().add(new SeparatorMenuItem());
  }

}

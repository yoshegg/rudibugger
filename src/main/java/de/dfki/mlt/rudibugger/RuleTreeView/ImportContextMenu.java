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

import de.dfki.mlt.rudibugger.RuleModel.ImportInfoExtended;
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

  /** The clicked Import. */
  private final ImportInfoExtended _item;

  /**
   * An <code>ImportContextMenu</code> should appear when a context menu was
   * requested by clicking on an import.
   *
   * @param ii
   */
  public ImportContextMenu(ImportInfoExtended ii) {
    super();
    _item = ii;
    initializeMenuItems();

    /* mark the current state */
    if (_item.getState() != STATE_PARTLY)
      RADIO_MENU_ITEMS.get(_item.getState()).setSelected(true);
    else
      PSEUDO_BUTTON.setSelected(true);
  }

  private void treatErrorInfo(ErrorInfo e) {
    String msg = e.getType().toString() + ": "
        + (e.getLocation().getBegin().getLine() + 1) + ":"
        + e.getLocation().getBegin().getColumn() + ": " + e.getMessage();
    Label label = new Label(msg);
    CustomMenuItem errorItem = new CustomMenuItem(label);
    Tooltip t = new Tooltip(e.getMessage());
    Tooltip.install(label, t);
    errorItem.setOnAction(f -> {
      _item.getModel().rudiLoad.openRule(_item.getAbsolutePath(),
          e.getLocation().getBegin().getLine() + 1);
    });
    this.getItems().add(errorItem);
  }

  /** Initializes MenuItems. */
  private void initializeMenuItems() {

    /* set open MenuItem and separator */
    CustomMenuItem openFile = new CustomMenuItem(new Label("Open "
            + _item.getAbsolutePath().getFileName().toString()));
    openFile.setOnAction((ActionEvent e) -> {
      _item.getModel().rudiLoad.openFile(_item.getAbsolutePath());
    });
    this.getItems().addAll(openFile, new SeparatorMenuItem());

    /* set possibility to open errors or warnings (if any) */
    if (_item.getModel().globalConf.showErrorInfoInRuleTreeViewContextMenu()) {
      for (ErrorInfo e : _item.getErrors()) {
        treatErrorInfo(e);
      }
      if (! _item.getErrors().isEmpty())
        this.getItems().add(new SeparatorMenuItem());
    }

    /* set RadioMenuButtons */
    ToggleGroup toggleGroup = new ToggleGroup();
    for (Integer s : RADIO_MENU_ITEMS.keySet()) {
      RADIO_MENU_ITEMS.get(s).setOnAction(e -> {
        _item.setAllChildrenStates(s);
      });
      this.getItems().add(RADIO_MENU_ITEMS.get(s));
      RADIO_MENU_ITEMS.get(s).setToggleGroup(toggleGroup);
    }

    /* add pseudo button */
    PSEUDO_BUTTON.setToggleGroup(toggleGroup);

  }
}

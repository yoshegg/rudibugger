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

import de.dfki.mlt.rudibugger.DataModelAdditions.GlobalConfiguration;
import de.dfki.mlt.rudibugger.Project.Project;
import de.dfki.mlt.rudibugger.Project.RuleModel.RuleInfoExtended;
import static de.dfki.mlt.rudimant.common.Constants.*;
import java.util.LinkedHashMap;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;

/**
 * This is the context menu appearing when making a right click on a rule in
 * the ruleTreeView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleContextMenu extends ContextMenu {

  /* ***************************************************************************
   * CONSTANTS
   * **************************************************************************/

  /**
   * Contains all the <code>RadioMenuItem</code>s of the
   * <code>ImportContextMenu</code>.
   */
  private static final LinkedHashMap<Integer, RadioMenuItem> RADIO_MENU_ITEMS
          = new LinkedHashMap<Integer, RadioMenuItem>() {{
      put(STATE_ALWAYS, new RadioMenuItem("Always log rule"));
      put(STATE_IF_TRUE, new RadioMenuItem("Log rule if true"));
      put(STATE_IF_FALSE, new RadioMenuItem("Log rule if false"));
      put(STATE_NEVER, new RadioMenuItem("Never log rule"));
    }};

  /**
   * Contains additional <code>MenuItem</code>s, used if a rule is subrules.
   */
  private static final LinkedHashMap<Integer, MenuItem> ADDITIONAL_MENU_ITEMS
          = new LinkedHashMap<Integer, MenuItem>() {{
      put(STATE_ALWAYS, new MenuItem("Always log rule and children"));
      put(STATE_IF_TRUE, new MenuItem("Log rule including children if true"));
      put(STATE_IF_FALSE, new MenuItem("Log rule including children if false"));
      put(STATE_NEVER, new MenuItem("Never log rule and children"));
    }};


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** The clicked Rule */
  private final RuleInfoExtended _item;

  private final Project _project;

  private final GlobalConfiguration _globalConf;


  /* ***************************************************************************
   * CONSTRUCTOR
   * **************************************************************************/

  /**
   * A <code>RuleContextMenu</code> should appear when a context menu was
   * requested by clicking on a rule.
   */
  public RuleContextMenu(RuleInfoExtended ri, Project project,
          GlobalConfiguration globalConf) {
    super();
    _item = ri;
    _project = project;
    _globalConf = globalConf;
    initializeMenuItems();

    /* mark the current state */
    RADIO_MENU_ITEMS.get(_item.getState()).setSelected(true);
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

 /** Initializes MenuItems */
  private void initializeMenuItems() {

    /* set open MenuItem */
    CustomMenuItem openRule = new CustomMenuItem(new Label("Open rule (line "
            + _item.getLine() + ")"));
    openRule.setOnAction((ActionEvent e) -> {
      _project.openRule(_item.getSourceFile(),
              _item.getLine());
    });
    SeparatorMenuItem sep = new SeparatorMenuItem();
    this.getItems().addAll(openRule, sep);

    /* set RadioMenuButtons */
    ToggleGroup toggleGroup = new ToggleGroup();
    for (Integer s : RADIO_MENU_ITEMS.keySet()) {
      RADIO_MENU_ITEMS.get(s).setOnAction(e -> {
        _item.setState(s);
      });
      this.getItems().add(RADIO_MENU_ITEMS.get(s));
      RADIO_MENU_ITEMS.get(s).setToggleGroup(toggleGroup);
    }

    /* if there are subrules, provide more options */
    if (! _item.getChildren().isEmpty()) {
      SeparatorMenuItem sep2 = new SeparatorMenuItem();
      Menu childrenMenu = new Menu("Subrules");

      for (Integer s: ADDITIONAL_MENU_ITEMS.keySet()) {
        ADDITIONAL_MENU_ITEMS.get(s).setOnAction(e -> {
          _item.setAllChildrenStates(s);
        });
        childrenMenu.getItems().add(ADDITIONAL_MENU_ITEMS.get(s));
//      TODO: WHAT IS HAPPENING HERE???
//      log.error("The warning above is simply not true.");
      }

      this.getItems().addAll(sep2, childrenMenu);
    }

  }
}
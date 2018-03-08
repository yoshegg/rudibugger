/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import static de.dfki.mlt.rudimant.common.Constants.*;
import java.util.LinkedHashMap;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;


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

  /** The clicked Import */
  private final ImportInfoExtended _item;

  /**
   * An <code>ImportContextMenu</code> should appear when a context menu was
   * requested by clicking on an import.
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

  /** Initializes MenuItems */
  private void initializeMenuItems() {

    /* set open MenuItem and separator */
    MenuItem openFile = new MenuItem("Open "
            + _item.getAbsolutePath().getFileName().toString());
    openFile.setOnAction((ActionEvent e) -> {
      _item._model.openFile(_item.getAbsolutePath());
    });
    SeparatorMenuItem sep = new SeparatorMenuItem();
    this.getItems().addAll(openFile, sep);

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

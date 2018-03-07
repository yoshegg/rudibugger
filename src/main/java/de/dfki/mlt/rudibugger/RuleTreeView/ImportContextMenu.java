/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import static de.dfki.mlt.rudimant.common.Constants.*;
import java.util.HashMap;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;


/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ImportContextMenu extends ContextMenu {

  /** Map of icons */
  static HashMap<Integer, CheckMenuItem> CHECK_MENU_ITEMS
    = new HashMap<Integer, CheckMenuItem>() {{
    put(STATE_ALWAYS, CMI_ALWAYS);
    put(STATE_IF_TRUE, CMI_IF_TRUE);
    put(STATE_IF_FALSE, CMI_IF_FALSE);
    put(STATE_NEVER, CMI_NEVER);
  }};

  /* the text of the different MenuItems */
  private static final CheckMenuItem CMI_ALWAYS
          = new CheckMenuItem("Aways log all child rules");
  private static final CheckMenuItem CMI_IF_TRUE
          = new CheckMenuItem("Log all child rules if true");
  private static final CheckMenuItem CMI_IF_FALSE
          = new CheckMenuItem("Log all child rules if false");
  private static final CheckMenuItem CMI_NEVER
          = new CheckMenuItem("Never log all child rules");

  /* the clicked ImportMenuItem */
  private final ImportInfoExtended _item;

  /* the constructor */
  public ImportContextMenu(ImportInfoExtended ii) {
    super();
    _item = ii;

    initializeMenuItems();
    retrieveState(_item.getState());
  }

  /* set logging MenuItems' ActionEvents */
  private void initializeMenuItems() {

    /* set open MenuItem */
    MenuItem openFile = new MenuItem("Open "
            + _item.getFilePath().getFileName().toString());
    openFile.setOnAction((ActionEvent e) -> {
      _item._model.openFile(_item.getFilePath());
    });

    SeparatorMenuItem sep = new SeparatorMenuItem();

    /* set actions when logging menu items are clicked */
    CMI_ALWAYS.setOnAction((ActionEvent e) -> {
      _item.setStateProperty(STATE_ALWAYS);
    });
    CMI_IF_TRUE.setOnAction((ActionEvent e) -> {
      _item.setStateProperty(STATE_IF_TRUE);
    });
    CMI_IF_FALSE.setOnAction((ActionEvent e) -> {
      _item.setStateProperty(STATE_IF_FALSE);
    });
    CMI_NEVER.setOnAction((ActionEvent e) -> {
      _item.setStateProperty(STATE_NEVER);
    });
    this.getItems().addAll(openFile, sep, CMI_ALWAYS, CMI_IF_TRUE,
            CMI_IF_FALSE, CMI_NEVER);
  }

  /* get the state from the TreeItem */
  private void retrieveState(Integer state) {
    CHECK_MENU_ITEMS.get(state).setSelected(true);
  }

}

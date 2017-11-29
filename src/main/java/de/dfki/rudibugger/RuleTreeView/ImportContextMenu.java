/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RuleTreeView;

import static de.dfki.rudibugger.Constants.*;
import static de.dfki.mlt.rudimant.common.Constants.*;
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

  /* the text of the different MenuItems */
  private final CheckMenuItem CMI_ALWAYS
          = new CheckMenuItem("Aways log all child rules");
  private final CheckMenuItem CMI_IF_TRUE
          = new CheckMenuItem("Log all child rules if true");
  private final CheckMenuItem CMI_IF_FALSE
          = new CheckMenuItem("Log all child rules if false");
  private final CheckMenuItem CMI_NEVER
          = new CheckMenuItem("Never log all child rules");

  /* the clicked ImportMenuItem */
  private final ImportTreeItem _item;

  /* the constructor */
  public ImportContextMenu(ImportTreeItem item) {
    super();
    _item = item;

    initializeMenuItems();
    retrieveState(_item.stateProperty().get());
  }

  /* set logging MenuItems' ActionEvents */
  private void initializeMenuItems() {

    /* set open MenuItem */
    MenuItem openFile = new MenuItem("Open "
            + _item.getFile().getFileName().toString());
    openFile.setOnAction((ActionEvent e) -> {
      _item._model.requestTabOfFile(_item.getFile());
    });

    SeparatorMenuItem sep = new SeparatorMenuItem();

    /* set actions when logging menu items are clicked */
    CMI_ALWAYS.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_ALWAYS);
    });
    CMI_IF_TRUE.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_IF_TRUE);
    });
    CMI_IF_FALSE.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_IF_FALSE);
    });
    CMI_NEVER.setOnAction((ActionEvent e) -> {
      _item.setState(STATE_NEVER);
    });
    this.getItems().addAll(openFile, sep, CMI_ALWAYS, CMI_IF_TRUE,
            CMI_IF_FALSE, CMI_NEVER);
  }

  /* get the state from the TreeItem */
  private void retrieveState(Integer state) {
    switch (state) {
      case STATE_ALWAYS:
        CMI_ALWAYS.setSelected(true);
        break;
      case STATE_IF_TRUE:
        CMI_IF_TRUE.setSelected(true);
        break;
      case STATE_IF_FALSE:
        CMI_IF_FALSE.setSelected(true);
        break;
      case STATE_NEVER:
        CMI_NEVER.setSelected(true);
        break;
    }
  }

}

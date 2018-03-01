/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import de.dfki.mlt.rudimant.common.RuleInfo;
import javafx.scene.control.TreeCell;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleTreeCell extends TreeCell<RuleInfo> {

  static final String ICON_PATH
          = "file:src/main/resources/icons/RudiLogRuleStatus/";

  @Override
  protected void updateItem(RuleInfo ri, boolean empty) {
    super.updateItem(ri, empty);

    if (empty || ri == null) {

      setText(null);
      setGraphic(null);

    } else {

      setText(rudiPath.toString());

      switch (rudiPath._usedProperty().getValue()) {
        case FILE_IS_MAIN:
          setGraphic(new ImageView(main));
          break;
        case FILE_IS_WRAPPER:
          setGraphic(new ImageView(wrapper));
          break;
        case FILE_USED:
          setGraphic(new ImageView(enabled));
          break;
        case FILE_NOT_USED:
          setGraphic(new ImageView(disabled));
          break;
        case IS_FOLDER:
          setGraphic(new ImageView(folder));
          break;
        default:
          break;
        }

}

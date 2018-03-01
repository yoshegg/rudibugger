/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import static de.dfki.mlt.rudimant.common.Constants.*;
import de.dfki.mlt.rudimant.common.ImportInfo;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ImportTreeCell extends TreeCell<ImportInfoExtended> {

  /** icon path */
  static final String ICON_PATH
          = "file:src/main/resources/icons/RudiLogFileStatus/";

  /* icon files */
  static Image imgAlways = new Image(ICON_PATH + "Always.png");
  static Image imgIfTrue = new Image(ICON_PATH + "IfTrue.png");
  static Image imgIfFalse = new Image(ICON_PATH + "IfFalse.png");
  static Image imgNever = new Image(ICON_PATH + "Never.png");
  static Image imgPartly = new Image(ICON_PATH + "Partly.png");

  /** Returns the requested folder icon */
  public static Image getImage(int state) {
    switch (state) {
      case STATE_ALWAYS:
        return imgAlways;
      case STATE_IF_TRUE:
        return imgIfTrue;
      case STATE_IF_FALSE:
        return imgIfFalse;
      case STATE_NEVER:
        return imgNever;
      case STATE_PARTLY:
        return imgPartly;
    }
    return null;
  }

  @Override
  protected void updateItem(ImportInfo ii, boolean empty) {
    super.updateItem(ii, empty);

    if (empty || ii == null) {

      setText(null);
      setGraphic(null);

    } else {

      setText(ii.toString());
      setGraphic(new ImageView(getImage(ii.)));
    }

}

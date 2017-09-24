/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.ruleTreeView;

import javafx.scene.image.Image;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ImportTreeItem extends BasicTreeItem {

  /* the different icons used as indicator */
  static final String FILE_ICON_PATH
          = "file:src/main/resources/icons/RudiLogFileStatus/";
  static Image imgAlways = new Image(FILE_ICON_PATH + "Always.png");
  static Image imgIfTrue = new Image(FILE_ICON_PATH + "IfTrue.png");
  static Image imgIfFalse = new Image(FILE_ICON_PATH + "IfFalse.png");
  static Image imgNever = new Image(FILE_ICON_PATH + "Never.png");
  static Image imgPartly = new Image(FILE_ICON_PATH + "Partly.png");

  /* the constructor */
  public ImportTreeItem(String importName) {
    super(importName);
  }

  /* returns the requested folder icon */
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
      default:
        return null;
    }
  }
}

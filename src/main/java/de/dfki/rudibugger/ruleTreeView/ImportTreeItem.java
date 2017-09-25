/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.ruleTreeView;

import java.nio.file.Path;
import javafx.scene.image.Image;
import static de.dfki.rudibugger.Constants.*;
import de.dfki.rudibugger.project.Project;
import java.nio.file.Paths;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ImportTreeItem extends BasicTreeItem {

  /* the associated file */
  private final Path _file;

  /* the different icons used as indicator */
  static final String FILE_ICON_PATH
          = "file:src/main/resources/icons/RudiLogFileStatus/";
  static Image imgAlways = new Image(FILE_ICON_PATH + "Always.png");
  static Image imgIfTrue = new Image(FILE_ICON_PATH + "IfTrue.png");
  static Image imgIfFalse = new Image(FILE_ICON_PATH + "IfFalse.png");
  static Image imgNever = new Image(FILE_ICON_PATH + "Never.png");
  static Image imgPartly = new Image(FILE_ICON_PATH + "Partly.png");

  /* the constructor */
  public ImportTreeItem(String importName, Project proj) {
    super(importName, proj);
    _file = Paths.get(project.getRootFolder() + "/"
            + PATH_TO_RUDI_FILES + importName + ".rudi");

    /* the specific context menu for rules */
    _hb.setOnContextMenuRequested((e) -> {
      ImportContextMenu contextMenu = new ImportContextMenu(this);
      contextMenu.show(_hb, e.getScreenX(), e.getScreenY());
    });
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

  /* get file */
  public Path getFile() {
    return _file;
  }
}

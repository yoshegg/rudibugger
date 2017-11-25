/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RuleTreeView;

import de.dfki.mlt.rudimant.common.ImportInfo;
import java.nio.file.Path;
import javafx.scene.image.Image;
import static de.dfki.rudibugger.Constants.*;
import de.dfki.rudibugger.DataModel;
import java.nio.file.Paths;
import javafx.scene.input.MouseButton;
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

  /** DataModel */
  public final DataModel _model;

  /* the constructor */
  public ImportTreeItem(ImportInfo content, DataModel model) {
    super(content);
    _model = model;
    _file = Paths.get(model.getRootFolder() + "/"
            + PATH_TO_RUDI_FILES + content.getLabel()+ RUDI_FILE_EXTENSION);
    defineControls();
  }

  private void defineControls() {
    /* the specific context menu for imports */
    _hb.setOnContextMenuRequested((e) -> {
      ImportContextMenu contextMenu = new ImportContextMenu(this);
      contextMenu.show(_hb, e.getScreenX(), e.getScreenY());
    });

    /* open File if clicked on label */
    _hb.getChildren().get(1).setOnMouseClicked((MouseEvent e) -> {
      if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
        _model.requestTabOfFile(_file);
      }
    });
  }

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
      default:
        return null;
    }
  }

  /** get file */
  public Path getFile() {
    return _file;
  }
}

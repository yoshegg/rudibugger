/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.FileTreeView;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static de.dfki.mlt.rudibugger.Constants.*;
import java.util.HashMap;
import java.util.Objects;
import javafx.css.PseudoClass;
import javafx.scene.control.TreeCell;

/**
 * This TreeCell is used to visualize the different .rudi files according to
 * their usage stage in the current project.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiTreeCell extends TreeCell<RudiPath> {

  /** Icon path of files. */
  static final String ICON_PATH_FILES
          = "file:src/main/resources/icons/FilesAndFolders/";

  /** Map of file icons. */
  static final HashMap<Integer, Image> ICONS_RULES = new HashMap<Integer, Image>() {{
    put(FILE_IS_MAIN,    new Image(ICON_PATH_FILES + "main.png"));
    put(FILE_IS_WRAPPER, new Image(ICON_PATH_FILES + "wrapper.png"));
    put(FILE_USED,       new Image(ICON_PATH_FILES + "enabled.png"));
    put(FILE_NOT_USED,   new Image(ICON_PATH_FILES + "disabled.png"));
    put(IS_FOLDER,       new Image(ICON_PATH_FILES + "folder.png"));
  }};

  /** Used to visually distinguish modified files with CSS */
  private final PseudoClass modifiedFileClass
          = PseudoClass.getPseudoClass("modifiedFile");

  @Override
  protected void updateItem(RudiPath rudiPath, boolean empty) {
    super.updateItem(rudiPath, empty);

    if (empty || rudiPath == null) {

      setText(null);
      setGraphic(null);

      pseudoClassStateChanged(modifiedFileClass, false);

    } else {

      /* Visually indicate out of sync files */
      pseudoClassStateChanged(modifiedFileClass,
              rudiPath._modifiedProperty().getValue());
//      TODO: Not working, JavaFX bug or simply not possible?
//      rudiPath._modifiedProperty().addListener((o, ov, nv) -> {
//        pseudoClassStateChanged(modifiedFileClass,
//                rudiPath._modifiedProperty().getValue());
//      });

      setText(rudiPath.toString());

      /* Define icon and listener for icon */
      setGraphic(new ImageView(ICONS_RULES.get(
              rudiPath._usedProperty().getValue())));
      rudiPath._usedProperty().addListener((o, oldVal, newVal) -> {
        if (oldVal != newVal)
          setGraphic(new ImageView(ICONS_RULES.get(newVal.intValue())));
      });

    }
  }
}

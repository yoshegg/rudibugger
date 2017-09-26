/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.project;

import java.nio.file.Path;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiFolderTreeItem extends TreeItem {

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /* the icon */
  static final String FILE_ICON_PATH
          = "file:src/main/resources/icons/FilesAndFolders/";
  static Image imgFolder = new Image(FILE_ICON_PATH + "folder.png");
  private final ImageView _folderIcon = new ImageView(){{
    this.setImage(imgFolder);
  }};

  /* the label of the TreeItem */
  private final Label _label;

  /* the constructor */
  public RudiFolderTreeItem(String label) {
    super();

    /* initialise label */
    _label = new Label(label);

    /* fill HBox */
    _hb = new HBox();
    this.setValue(_hb);
    _hb.getChildren().add(_folderIcon);
    _hb.getChildren().add(_label);
    _hb.setAlignment(Pos.CENTER_LEFT);
  }

  /* the HBox of the TreeItem */
  private final HBox _hb;


}

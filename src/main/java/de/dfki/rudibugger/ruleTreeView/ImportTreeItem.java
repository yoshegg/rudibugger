/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.ruleTreeView;

import static de.dfki.rudibugger.ruleTreeView.BasicTreeItem.RULE_ICON_PATH;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ImportTreeItem extends BasicTreeItem {

  static Image imgNever = new Image(RULE_ICON_PATH + "file.png");


  public ImportTreeItem(String importName, BasicTreeItem parent) {
    super(importName, parent);
    ImageView folderIcon = new ImageView();
    folderIcon.setImage(imgNever);
    _hb.getChildren().add(0, folderIcon);
  }

  public ImportTreeItem(String importName) {
    this(importName, null);
  }
}

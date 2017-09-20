/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.rudibugger.project;

import java.io.File;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiFileTreeItem extends TreeItem {

  public RudiFileTreeItem(Object label) {
    super(label);
  }

  private String path;

  public void setFile(String path) {
    this.path = path;
  }

  public File getFile() {
    return new File(path);
  }

}

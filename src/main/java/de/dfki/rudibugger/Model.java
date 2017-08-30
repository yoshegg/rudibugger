/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.rudibugger;

import de.dfki.rudibugger.folderstructure.RudiTreeItem;
import de.dfki.rudibugger.tabs.RudiTab;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Model {

  static Logger log = Logger.getLogger("rudiLog");

  public Stage stageX;

  public File projectX;

  public void openFile(TabPane tabpanex) throws FileNotFoundException {
    log.debug("Opening file chooser...");
    FileChooser fileChooser = new FileChooser();

    // Set extension filter
    FileChooser.ExtensionFilter extFilterRUDI = new FileChooser.ExtensionFilter("RUDI files (*.rudi)", "*.rudi", "*.RUDI");
    FileChooser.ExtensionFilter extFilterALL = new FileChooser.ExtensionFilter("All files", "*.*");
    fileChooser.getExtensionFilters().addAll(extFilterRUDI);
    fileChooser.getExtensionFilters().addAll(extFilterALL);

    fileChooser.setTitle("Open .rudi file(s)");
    List selectedFiles = fileChooser.showOpenMultipleDialog(stageX);
    if (selectedFiles == null) {
      log.debug("Aborted selection of .rudi file(s)");
      return;
    }
    // iterate over chosen files and open them in a new tab
    for (Object file : selectedFiles) {
      RudiTab tab = new RudiTab(tabpanex, (File) file);
    }
  }

  // https://stackoverflow.com/questions/35070310/javafx-representing-directories
  public void openProject(TreeView treeviewx) {
    log.debug("Opening project chooser...");
    DirectoryChooser dc = new DirectoryChooser();
    dc.setInitialDirectory(new File(System.getProperty("user.home")));
    dc.setTitle("Open project directory");
    File choice = dc.showDialog(stageX);
    if (choice == null) {
      log.debug("Aborted selection of project directory");
    } else {
      projectX = choice;
      log.info("Chose " + projectX.getAbsolutePath() + " as project's path.");
      treeviewx.setRoot(getNodesForDirectory(choice));
      treeviewx.getRoot().setExpanded(true);
    }
  }

  /*
   * Returns a TreeItem representation of the specified directory
   */
  public TreeItem<String> getNodesForDirectory(File directory) {
    TreeItem<String> root = new TreeItem<String>(directory.getName());
    for (File f : directory.listFiles()) {
      if (f.isDirectory()) { //Then we call the function recursively
        root.getChildren().add(getNodesForDirectory(f));
      } else {
        if (f.getName().contains(".")) {
          String fileEnding = f.getName().substring(f.getName().lastIndexOf('.'));
          if (".rudi".equals(fileEnding)) {
            RudiTreeItem item = new RudiTreeItem(f.getName());
            item.setFile(f.getAbsolutePath());
            root.getChildren().add(item);
          }
        }
      }
    }
    return root;
  }


}

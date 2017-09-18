/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.rudibugger;

import static de.dfki.rudibugger.FXMLController.log;
import de.dfki.rudibugger.project.Project;
import static de.dfki.rudibugger.project.Project.*;
import de.dfki.rudibugger.project.RudiTreeItem;
import de.dfki.rudibugger.tabs.RudiTab;
import java.awt.GraphicsEnvironment;
import java.net.URISyntaxException;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

  public Project projectX;

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

  /**
   *
   * @param treeFiles
   * @param treeRules
   * @return true, if new project has been opened, false otherwise
   */
  public boolean openProjectYml(TreeView treeFiles, TreeView treeRules) {
    log.debug("Opening project chooser (yml)");
    FileChooser yml = new FileChooser();
    yml.setInitialDirectory(new File(System.getProperty("user.home")));
    yml.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("YAML files (*.yml)", "*.yml", "*.YML"));
    yml.setTitle("Open project .yml");
    File ymlFile = yml.showOpenDialog(stageX);
    if (ymlFile == null) {
      log.debug("Aborted selection of .yml file");
      return false;
    }
    if (projectX != null) {
      log.info("Closing old project [" + projectX.getProjectName() + "]");
      clearProject();
    }
    projectX = Project.initProject(ymlFile);
    return true;
  }

  // https://stackoverflow.com/questions/35070310/javafx-representing-directories
  public void openProjectDirectoryChooser(TreeView treeviewx) {
    log.debug("Opening project chooser (directory)");
    DirectoryChooser dc = new DirectoryChooser();
    dc.setInitialDirectory(new File(System.getProperty("user.home")));
    dc.setTitle("Open project directory");
    File choice = dc.showDialog(stageX);
    if (choice == null) {
      log.debug("Aborted selection of project directory");
    } else {
      projectX = setDirectory(choice);
      log.info("Chose " + projectX.getRootFolderPath() + " as project's path.");
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

  public void newEmptyTab(TabPane tabpane) throws FileNotFoundException {
    new RudiTab(tabpane);
  }

  public void startCompile() throws IOException, InterruptedException {
    log.info("Starting compilation...");
    if ("Linux".equals(System.getProperty("os.name"))) {
      Process p = Runtime.getRuntime()
              .exec("/usr/bin/xterm " + projectX.getCompileFile().toString());
    } else {
      Process p = Runtime.getRuntime().exec(projectX.getCompileFile().toString());
    }
  }
}

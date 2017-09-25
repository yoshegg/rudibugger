/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.rudibugger;

import de.dfki.rudibugger.project.Project;
import de.dfki.rudibugger.project.RudiFileTreeItem;
import de.dfki.rudibugger.tabs.RudiTab;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Model {

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /* the main stage */
  public Stage stageX;

  /* the only yaml instance */
  public Yaml yaml;

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
      RudiTab tab = new RudiTab(tabpanex, (Path) file);
    }
  }

  /**
   *
   * @param treeFiles
   * @param treeRules
   * @return true, if new project has been opened, false otherwise
   */
  public boolean openProjectYml(TreeView treeFiles, TreeView treeRules)
          throws FileNotFoundException, IOException {
    log.debug("Opening project chooser (yml)");
    FileChooser yml = new FileChooser();
    yml.setInitialDirectory(new File(System.getProperty("user.home")));
    yml.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("YAML files (*.yml)", "*.yml", "*.YML"));
    yml.setTitle("Open project .yml");
    File ymlFile2 = yml.showOpenDialog(stageX);

    if (ymlFile2 == null) {
      log.debug("Aborted selection of .yml file");
      return false;
    }
    if (projectX != null) {
      log.info("Closing old project [" + projectX.getProjectName() + "]");
//      clearProject();
    }
    Path ymlFile = ymlFile2.toPath();
    return processOpeningProjectYml(ymlFile, treeFiles, treeRules);
  }

  public boolean processOpeningProjectYml(Path ymlFile, TreeView treeFiles, TreeView treeRules)
          throws FileNotFoundException, IOException {
    projectX = new Project(ymlFile, yaml);
    projectX.initProject();
    projectX.setRuleTreeView(treeRules);

    TreeItem<String> rudiRoot = getNodesForDirectory(projectX.getRudisFolder());
    rudiRoot.setValue(projectX.getProjectName() + " - .rudi files");
    treeFiles.setRoot(rudiRoot);
    treeFiles.getRoot().setExpanded(true);

    /* retrieve ruleLocMap and show if possible */
    if (projectX.getRuleLocFile() != null) {
      projectX.retrieveRuleLocMap();
    }
    return true;
  }

  // https://stackoverflow.com/questions/35070310/javafx-representing-directories
  public void openProjectDirectoryChooser(TreeView treeviewx) throws IOException {
    log.debug("Opening project chooser (directory)");
    DirectoryChooser dc = new DirectoryChooser();
    dc.setInitialDirectory(new File(System.getProperty("user.home")));
    dc.setTitle("Open project directory");
    File choice2 = dc.showDialog(stageX);
    if (choice2 == null) {
      log.debug("Aborted selection of project directory");
    } else {
//      clearProject();
      Path choice = choice2.toPath();
      projectX = new Project();
      projectX.setDirectory(choice);
      log.info("Chose " + projectX.getRootFolder() + " as project's path.");
      treeviewx.setRoot(getNodesForDirectory(choice));
      treeviewx.getRoot().setExpanded(true);
    }
  }

  /*
   * Returns a TreeItem representation of the specified directory
   */
  public TreeItem<String> getNodesForDirectory(Path directory) throws IOException {
    TreeItem<String> root = new TreeItem<>(directory.toString());
    DirectoryStream<Path> stream = Files.newDirectoryStream(directory);
    for (Path f : stream) {
      if (Files.isDirectory(f)) { //Then we call the function recursively
        root.getChildren().add(getNodesForDirectory(f));
      } else {
        if (f.toString().toLowerCase().endsWith(".rudi")) {
          RudiFileTreeItem item = new RudiFileTreeItem(f.getFileName().toString());
          item.setFile(f);
          root.getChildren().add(item);
        }
      }
    }
    return root;
  }

  public void newEmptyTab(TabPane tabpane) throws FileNotFoundException {
    new RudiTab(tabpane);
  }

  public void startCompile(TreeView treeRules) throws IOException, InterruptedException {
    log.info("Starting compilation...");
    Process p;
    String compileScript = projectX.getCompileFile().toString();
    String[] cmd = { "/usr/bin/xterm", "-e", compileScript, "-b"};
    log.debug("Executing the following command: " + Arrays.toString(cmd));
    if ("Linux".equals(System.getProperty("os.name"))) {
      p = Runtime.getRuntime().exec(cmd);
    } else {
      p = Runtime.getRuntime().exec(projectX.getCompileFile().toString() + "-b");
    }
  }
}

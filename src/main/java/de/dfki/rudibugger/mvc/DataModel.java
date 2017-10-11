/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.mvc;

import de.dfki.rudibugger.HelperWindows;
import de.dfki.rudibugger.project.Project;
import de.dfki.rudibugger.project.RudiFileTreeItem;
import de.dfki.rudibugger.project.RudiFolderTreeItem;
import de.dfki.rudibugger.tabs.RudiHBox;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class DataModel {

  /** the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /** the main stage */
  public Stage stageX;

  /** the only yaml instance */
  public Yaml yaml;


  /* TODO: Remove? */
  public Project projectX;

  /* TODO: comment */
  private final ObservableList<Rule> ruleList = FXCollections.observableArrayList();

  public final ObservableList<Rule> getRuleMap() {
    return ruleList;
  }


  /* statusBar */
  private StringProperty statusBar;
  public void setStatusBar(String value) { statusBarProperty().set(value); }
  public String getStatusBar() { return statusBarProperty().get(); }
  public StringProperty statusBarProperty() {
      if (statusBar == null) statusBar = new SimpleStringProperty(this, "firstName");
      return statusBar;
  }



  /* OLD */

  /**
   *
   * @param treeFiles
   * @param treeRules
   * @param tabPaneBack
   * @return true, if new project has been opened, false otherwise
   * @throws java.io.IOException
   */
  public boolean openProjectYml(TreeView treeFiles, TreeView treeRules,
          RudiHBox tabPaneBack) throws IOException {
    Path ymlFile = HelperWindows.openYmlProjectFile(stageX);
    return processProjectYml(ymlFile, treeFiles, treeRules, tabPaneBack);
  }

  public boolean processProjectYml(Path ymlFile, TreeView treeFiles,
          TreeView treeRules, RudiHBox tabPaneBack)
          throws FileNotFoundException, IOException {
    projectX = new Project(ymlFile, yaml);
    projectX.initProject();
    projectX.setRuleTreeView(treeRules);
    projectX.setFileTreeView(treeFiles);
    projectX.setRudiHBox(tabPaneBack);

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

  /*
   * Returns a TreeItem representation of the specified directory
   */
  public RudiFolderTreeItem getNodesForDirectory(Path directory) throws IOException {
    RudiFolderTreeItem root = new RudiFolderTreeItem(directory.getFileName().toString());

    /* get a sorted list of this directory's files */
    DirectoryStream<Path> stream = Files.newDirectoryStream(directory);
    List<Path> list = new ArrayList<>();
    stream.forEach(list::add);
    list.sort(
      (Path h1, Path h2) -> h1.getFileName().toString().toLowerCase()
              .compareTo(h2.getFileName().toString().toLowerCase()));

    /* iterate over the found files */
    for (Path f : list) {
      /* if we find another directory, we call the function recursively */
      if (Files.isDirectory(f)) {
        root.getChildren().add(getNodesForDirectory(f));
      }
      /* else we make sure to only have .rudi files */
      else {
        if (f.toString().toLowerCase().endsWith(".rudi")) {
          RudiFileTreeItem item = new RudiFileTreeItem(f.getFileName().toString());
          item.setFile(f);
          root.getChildren().add(item);
        }
      }
    }
    return root;
  }

  public void newRudiFile() throws FileNotFoundException {
    projectX.getRudiHBox().getNewEmptyTab();
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

  public void createNewProject() {
    log.info("Not implemented yet.");
  }

}

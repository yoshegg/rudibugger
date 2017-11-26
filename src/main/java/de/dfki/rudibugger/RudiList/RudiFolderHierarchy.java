/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RudiList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiFolderHierarchy {

  public TreeItem _root;

  Path _rudiFolder;

  HashMap<Path, TreeItem> folderMap;
  HashMap<Path, TreeItem> fileMap;

  public RudiFolderHierarchy(Path rudiFolder) {
    _rudiFolder = rudiFolder;
    folderMap = new HashMap<>();
    fileMap = new HashMap<>();
  }

  /** add a file or a folder to the hierarchy */
  public boolean addFileToHierarchy(RudiPath file) {
    Path f = file.getPath();

    /* a folder */
    if (Files.isDirectory(f)) {
      TreeItem ti = new TreeItem(file);
      if (f.equals(_rudiFolder)) {
        _root = ti;
      }
      Path dir = f.subpath(0, f.getNameCount());
      folderMap.put(dir, ti);

      /* link to the parent folder's TreeItem */
      if (! f.equals(_rudiFolder)) {
        Path parentDir = f.subpath(0, f.getNameCount()-1);
        System.out.println(folderMap);
        folderMap.get(parentDir).getChildren().add(ti);

        /* sort the TreeItems, TODO: not efficient */
        ObservableList<TreeItem> children = folderMap.get(parentDir).getChildren();
        children.sort(
                (TreeItem h1, TreeItem h2)
                -> ((RudiPath) h1.getValue()).getPath().getFileName().toString().toLowerCase()
                        .compareTo(((RudiPath) h2.getValue()).getPath().getFileName().toString().toLowerCase()));
      }

    /* a file */
    } else {
      Path dir = f.subpath(0, f.getNameCount()-1);
      TreeItem ti = new TreeItem(file);
      folderMap.get(dir).getChildren().add(ti);
      fileMap.put(f, ti);

      /* sort the TreeItems, TODO: not efficient */
      ObservableList<TreeItem> children = folderMap.get(dir).getChildren();
      children.sort(
              (TreeItem h1, TreeItem h2)
              -> ((RudiPath) h1.getValue()).getPath().getFileName().toString().toLowerCase()
                      .compareTo(((RudiPath) h2.getValue()).getPath().getFileName().toString().toLowerCase()));
    }
    return true;
  }

  /** this only works for files */
  public void removeFromFileHierarchy(RudiPath file) {
    Path f = file.getPath();

    /* a file */
    if (! Files.isDirectory(f)) {
      fileMap.get(f).getParent().getChildren().remove(fileMap.get(f));
    }
  }

  public void removeObsoleteFolders() throws IOException {
    Stream<Path> stream = Files.walk(_rudiFolder);
    Set<Path> knownFolders = new HashSet(folderMap.keySet());
    Set<Path> actualFolders = new HashSet<>();
    stream.forEach(x -> {
      if (Files.isDirectory(x)) {
        actualFolders.add(x.subpath(0, x.getNameCount()));
      }
    });
    knownFolders.removeAll(actualFolders);

    for (Path y : knownFolders) {
      folderMap.get(y).getParent().getChildren().remove(folderMap.get(y));
      folderMap.remove(y);
    }
  }

}

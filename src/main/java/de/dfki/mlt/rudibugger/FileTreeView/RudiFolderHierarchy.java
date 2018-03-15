/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.FileTreeView;

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
 * RudiFolderHierarchy manages the underlying model of the rudiTreeView.
 * When a file or a folder is deleted or added, the hierarchy is immediately
 * updated so that the rudiTreeView is adapted.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiFolderHierarchy {

  /** this item represents the root of the rudi folder */
  public TreeItem _root;

  private final Path _rudiFolder;
  private final HashMap<Path, TreeItem> folderMap;
  private final HashMap<Path, TreeItem> fileMap;
  public final HashSet<RudiPath> rudiPathSet;
  public final HashMap<Path, RudiPath> rudiPathMap;

  /** create a new hierarchy and initialize its fields */
  public RudiFolderHierarchy(Path rudiFolder) {
    _rudiFolder = rudiFolder;
    folderMap = new HashMap<>();
    fileMap = new HashMap<>();
    rudiPathSet = new HashSet<>();
    rudiPathMap = new HashMap<>();
  }

  public void resetModifiedProperties() {
    for (Path p : rudiPathMap.keySet()) {
      rudiPathMap.get(p)._modifiedProperty().setValue(false);
    }
  }

  /** add a file or a folder to the hierarchy */
  public boolean addFileToHierarchy(RudiPath file) {
    Path f = file.getPath();

    rudiPathMap.put(f, file);

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
        folderMap.get(parentDir).getChildren().add(ti);

        /* sort the TreeItems, TODO: not efficient, but no easier way exists */
        ObservableList<TreeItem> children = folderMap.get(parentDir).getChildren();
        children.sort(
                (TreeItem h1, TreeItem h2)
                -> ((RudiPath) h1.getValue()).getPath().getFileName().toString().toLowerCase()
                        .compareTo(((RudiPath) h2.getValue()).getPath().getFileName().toString().toLowerCase()));
      }

    /* a file */
    } else {
      rudiPathSet.add(file);
      Path dir = f.subpath(0, f.getNameCount()-1);
      TreeItem ti = new TreeItem(file);
      folderMap.get(dir).getChildren().add(ti);
      fileMap.put(f, ti);

      /* sort the TreeItems, TODO: not efficient, but no easier way exists */
      ObservableList<TreeItem> children = folderMap.get(dir).getChildren();
      children.sort(
              (TreeItem h1, TreeItem h2)
              -> ((RudiPath) h1.getValue()).getPath().getFileName().toString().toLowerCase()
                      .compareTo(((RudiPath) h2.getValue()).getPath().getFileName().toString().toLowerCase()));
    }
    return true;
  }

  /** remove a file from the hierarchy (this only works for files!) */
  public void removeFromFileHierarchy(RudiPath file) {
    Path f = file.getPath();

    rudiPathMap.remove(f);

    /* a file */
    if (! Files.isDirectory(f)) {
      rudiPathSet.remove(file);
      fileMap.get(f).getParent().getChildren().remove(fileMap.get(f));
    }
  }

  /** check for deleted folders and remove them from the hierarchy */
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

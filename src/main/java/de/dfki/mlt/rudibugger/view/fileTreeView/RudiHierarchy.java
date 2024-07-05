/*
 * The Creative Commons CC-BY-NC 4.0 License
 *
 * http://creativecommons.org/licenses/by-nc/4.0/legalcode
 *
 * Creative Commons (CC) by DFKI GmbH
 *  - Bernd Kiefer <kiefer@dfki.de>
 *  - Anna Welker <anna.welker@dfki.de>
 *  - Christophe Biwer <christophe.biwer@dfki.de>
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.dfki.mlt.rudibugger.view.fileTreeView;

import static de.dfki.mlt.rudibugger.Constants.*;
import static de.dfki.mlt.rudimant.common.Constants.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RudiHierarchy manages the underlying data of the rudiTreeView. This class
 * also contains properties to listen to if changes to the file system must be
 * noticed.
 *
 * When a file or a folder is deleted or added, the hierarchy is immediately
 * updated so that the rudiTreeView is adapted.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiHierarchy {

  static Logger log = LoggerFactory.getLogger("RudiFolderHierarchy");

  /** Represents the folder containing the .rudi files. */
  private final Path _rudiFolder;

  /** Represents the project's RuleLoc.yml. */
  private final Path _ruleLocYaml;







  /** The root <code>TreeItem</code> of the Hierarchy. */
  private TreeItem _root;

  /** Maps folders to their respective <code>TreeItem</code>. */
  private final HashMap<Path, TreeItem> _folderMap = new HashMap<>();

  /** Maps files to their respective <code>TreeItem</code>. */
  private final HashMap<Path, TreeItem> _fileMap = new HashMap<>();

  /** Contains all known <code>.rudi</code> files and folders. */
  private final HashSet<RudiPath> _rudiPathSet = new HashSet<>();

  /** Maps a file / folder to its internal rudibugger representation. */
  private final HashMap<Path, RudiPath> _rudiPathMap = new HashMap<>();

  /** Indicates if there were modifications after the last compilation. */
  private final IntegerProperty _modificationsAfterCompilation
          = new SimpleIntegerProperty(FILES_SYNC_NO_PROJECT);


  /* ***************************************************************************
   * INITIALIZERS, UPDATERS AND RESETTER
   * **************************************************************************/

  /**
   * TODO
   */
  public RudiHierarchy(Path rudiFolder, Path ruleLocYaml) {
    log.debug("Initializing the RudiFolderHierarchy...");
    _rudiFolder = rudiFolder;
    _ruleLocYaml = ruleLocYaml;
    readInRudiFiles();
    log.debug("Initialized the RudiFolderHierarchy.");
  }

  /** Resets the rudiHierarchy. */
  public void reset() {
    log.debug("Resetting the RudiFolderHierarchy...");
    _root = null;
    _fileMap.clear();
    _folderMap.clear();
    _rudiPathMap.clear();
    _rudiPathSet.clear();
    _modificationsAfterCompilation.set(FILES_SYNC_NO_PROJECT);
    log.debug("Resetted the RudiFolderHierarchy.");
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /** Sorts the <code>TreeItem</code>s based on their lowercased file name. */
  class RudiComparator implements Comparator<TreeItem> {
    @Override
    public int compare(TreeItem ti1, TreeItem ti2) {
      String item1 = ((RudiPath) ti1.getValue()).getPath().getFileName()
              .toString().toLowerCase();
      String item2 = ((RudiPath) ti2.getValue()).getPath().getFileName()
              .toString().toLowerCase();
      return item1.compareTo(item2);
    }
  }

  /**
   * Reads in all <code>.rudi</code> files from the project's rudiFolder and
   * adds them to the hierarchy.
   */
  private void readInRudiFiles() {
    Stream<Path> stream;
    try {
      stream = Files.walk(_rudiFolder);
    } catch (IOException e) {
      log.error(e.toString());
      return;
    }
    stream.forEach(x -> {
      try {
        if ((x.getFileName().toString().endsWith(RULE_FILE_EXTENSION)
                || Files.isDirectory(x)) && ! Files.isHidden(x))
          addFileToHierarchy(x.toAbsolutePath());
      } catch (IOException e) {
        log.error(e.toString());
      }
    });

    /* Check if files are in sync */
    Boolean notSynced = false;
    for (Path p : _rudiPathMap.keySet()) {
      if (_rudiPathMap.get(p).modifiedProperty().get()) {
        notSynced = true;
      }
    }
    if (notSynced) modificationsAfterCompilationProperty()
              .set(FILES_OUT_OF_SYNC);
    else modificationsAfterCompilationProperty()
              .set(FILES_SYNCED);
  }

  /**
   * Adds a newly appeared file to the rudiHierarchy.
   *
   * @param f
   *        A <code>.rudi</code> file or folder
   */
  public void addFileToHierarchy(Path f) {
    RudiPath rp = new RudiPath(f);
    _rudiPathMap.put(f, rp);


    /* A folder */
    if (Files.isDirectory(f)) {
      TreeItem ti = new TreeItem(rp);
      if (f.equals(_rudiFolder)) {
        _root = ti;
      }
      _folderMap.put(f, ti);

      /* link to the parent folder's TreeItem */
      if (! f.equals(_rudiFolder)) {
        Path parentDir = f.getParent();
        _folderMap.get(parentDir).getChildren().add(ti);

        /* sort the TreeItems, TODO: not efficient, but no easier way exists */
        ObservableList<TreeItem> children = _folderMap.get(parentDir).getChildren();
        children.sort(new RudiComparator());
      }
    }

    /* A file */
    else {
      _rudiPathSet.add(rp);
      Path dir = f.getParent();
      TreeItem ti = new TreeItem(rp);
      _folderMap.get(dir).getChildren().add(ti);
      _fileMap.put(f, ti);

      /* Check if modified since last compilation */
      if (Files.exists(_ruleLocYaml))
        if (rp.getPath().toFile().lastModified() >
                _ruleLocYaml.toFile().lastModified())
          rp.modifiedProperty().set(true);


      /* sort the TreeItems, TODO: not efficient, but no easier way exists */
      ObservableList<TreeItem> children = _folderMap.get(dir).getChildren();
      children.sort(new RudiComparator());
    }
  }

  /**
   * Removes a file from the hierarchy (only working for files).
   *
   * @param f
   *        A <code>.rudi</code> file or folder
   */
  public void removeFromFileHierarchy(Path f) {

    RudiPath rp = new RudiPath(f);
    _rudiPathMap.remove(f);

    /* a file */
    if (! Files.isDirectory(f)) {
      _rudiPathSet.remove(rp);
      _fileMap.get(f).getParent().getChildren().remove(_fileMap.get(f));
    }
  }

  /** Checks for deleted folders and removes them from the hierarchy */
  public void removeObsoleteFolders() throws IOException {
    Stream<Path> stream = Files.walk(_rudiFolder);
    Set<Path> knownFolders = new HashSet(_folderMap.keySet());
    Set<Path> actualFolders = new HashSet<>();
    stream.forEach(x -> {
      if (Files.isDirectory(x)) {
        actualFolders.add(x);
      }
    });
    knownFolders.removeAll(actualFolders);

    for (Path y : knownFolders) {
      _folderMap.get(y).getParent().getChildren().remove(_folderMap.get(y));
      _folderMap.remove(y);
      _rudiPathMap.remove(y);
    }
    for (RudiPath p : new HashSet<>(_rudiPathSet)) {
      if (!Files.exists(p.getPath())) {
        removeFromFileHierarchy(p.getPath());
      }
    }
  }

  /** Sets a file as modified since last compilation. */
  public void setFileAsModified(Path file) {
    _rudiPathMap.get(file).modifiedProperty().setValue(true);
    if (_modificationsAfterCompilation.get() != FILES_OUT_OF_SYNC)
      _modificationsAfterCompilation.setValue(FILES_OUT_OF_SYNC);
  }

  /** Sets all files as in sync with compiled code. */
  public void setFilesUpToDate() {
    resetFilesModifiedProperties();
    _modificationsAfterCompilation.setValue(FILES_SYNCED);
  }

  /** Sets all files as not modified. */
  public void resetFilesModifiedProperties() {
    _rudiPathMap.keySet().forEach((p) -> {
      _rudiPathMap.get(p).modifiedProperty().setValue(false);
    });
  }

  /** @return True, if file is already known, else false */
  public boolean isFileInHierarchy(Path file) {
    return _rudiPathMap.keySet().contains(file);
  }


  /* ***************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS AND PROPERTIES
   * **************************************************************************/

  /** Indicates if there were modifications after the last compilation. */
  public IntegerProperty modificationsAfterCompilationProperty() {
    return _modificationsAfterCompilation;
  }

  /** @return The root <code>TreeItem</code> of the Hierarchy */
  public TreeItem getRoot() { return _root; }

  /** @return A map containing all known files and folders. */
  public HashSet<RudiPath> getRudiPathSet() { return _rudiPathSet; }

}

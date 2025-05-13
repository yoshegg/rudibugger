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
import static de.dfki.mlt.rudimant.common.Constants.RULE_FILE_EXTENSION;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

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
  private TreeItem<RudiPath> _root;

  /** Maps files and folders to their respective <code>TreeItem</code>. */
  private final HashMap<Path, TreeItem<RudiPath>> _fileMap = new HashMap<>();


  /** Indicates if there were modifications after the last compilation. */
  private final IntegerProperty _modificationsAfterCompilation
          = new SimpleIntegerProperty(FILES_SYNC_NO_PROJECT);


  /* ***************************************************************************
   * INITIALIZERS, UPDATERS AND RESETTER
   * **************************************************************************/

  /**
   */
  public RudiHierarchy(Path rudiFolder, Path ruleLocYaml) {
    log.debug("Initializing the RudiFolderHierarchy...");
    _rudiFolder = rudiFolder;
    _ruleLocYaml = ruleLocYaml;
    log.debug("Initialized the RudiFolderHierarchy.");
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /** Sorts the <code>TreeItem</code>s based on their lowercased file name. */
  class RudiComparator implements Comparator<TreeItem<RudiPath>> {
    @Override
    public int compare(TreeItem<RudiPath> ti1, TreeItem<RudiPath> ti2) {
      String item1 = ti1.getValue().getPath().getFileName()
              .toString().toLowerCase();
      String item2 = ti2.getValue().getPath().getFileName()
              .toString().toLowerCase();
      return item1.compareTo(item2);
    }
  }


  private TreeItem<RudiPath> getTreeItem(Path f) {
    RudiPath rp = new RudiPath(f);
    TreeItem<RudiPath> ti = new TreeItem<>(rp);
    _fileMap.put(f, ti);
    return ti;
  }

  private void treatParentDir(Path f, TreeItem<RudiPath> ti, List<Path> newDirs) {
    Path parentDir = f.getParent();
    addDirectory(parentDir, newDirs);
    _fileMap.get(parentDir).getChildren().add(ti);

    /* sort the TreeItems, TODO: not efficient, but no easier way exists */
    ObservableList<TreeItem<RudiPath>> children =
        _fileMap.get(parentDir).getChildren();
    children.sort(new RudiComparator());
  }


  /**
   * Adds a newly appeared folder to the rudiHierarchy.
   *
   * @param f
   *        A folder
   */
  private void addDirectory(Path f, List<Path> newDirs) {
    assert Files.isDirectory(f);
    if (_fileMap.containsKey(f)) return;
    newDirs.add(f);
    TreeItem<RudiPath> ti = getTreeItem(f);

    if (f.equals(_rudiFolder)) {
      _root = ti;
    } else { // (! f.equals(_rudiFolder))
      /* link to the parent folder's TreeItem */
      treatParentDir(f, ti, newDirs);
    }
  }

  private void addFile(Path f, List<Path> newDirs) {
    assert ! Files.isDirectory(f);
    if (_fileMap.containsKey(f)) {
      log.debug("rudi file has possibly been modified: {}", f);
      setFileAsModified(_fileMap.get(f));
    } else {
      log.debug("rudi file added: {}", f);
      TreeItem<RudiPath> ti = getTreeItem(f);

      /* Check if modified since last compilation */
      if (Files.exists(_ruleLocYaml)) {
        if (f.toFile().lastModified() > _ruleLocYaml.toFile().lastModified()) {
          setFileAsModified(ti);
        }
      }
      treatParentDir(f, ti, newDirs);
    }
  }


  /**
   * Removes a file from the hierarchy (only working for files).
   *
   * @param f
   *        A <code>.rudi</code> file or folder
   */
  private void removeFromHierarchy(Path f) {
    TreeItem<RudiPath> fileItem = null;
    if (_fileMap.containsKey(f)) {
      fileItem = _fileMap.get(f);
    }

    if (fileItem != null) {
      _fileMap.remove(f);
      fileItem.getParent().getChildren().remove(fileItem);
    } else {
      log.warn("Trying to remove file not in hierarchy {}", f);
    }
  }


  /** Sets a file as modified since last compilation. */
  private void setFileAsModified(TreeItem<RudiPath> ti) {
    ti.getValue().modifiedProperty().setValue(true);
    if (_modificationsAfterCompilation.get() != FILES_OUT_OF_SYNC)
      _modificationsAfterCompilation.setValue(FILES_OUT_OF_SYNC);
  }

  /** Sets all files as not modified. */
  private void resetFilesModifiedProperties() {
    _fileMap.values().forEach((ti) -> {
      ti.getValue().modifiedProperty().setValue(false);
    });
  }


  /* ***************************************************************************
   * PUBLIC METHODS
   * **************************************************************************/

  public static boolean isRudiFile(Path p) {
    try {
      return ! Files.isHidden(p) &&
        p.getFileName().toString().endsWith(RULE_FILE_EXTENSION);
    }
    catch (Exception ex) {
      log.error("Unexpected File Exception: {}", ex);
    }
    return false;
  }


  /**
   * Adds a newly appeared file to the rudiHierarchy, or marks it as modified,
   * if it's already in the hierarchy
   *
   * @param f
   *        A <code>.rudi</code> file
   */
  public void fileAddedOrModified(Path f) {
    // no new directories will be there in this case, only for WatchService
    addFile(f, new ArrayList<Path>());
  }


  public Iterable<Path> addDirectoryRecursively(Path path) throws IOException {
    List<Path> newDirs = new ArrayList<Path>();
    try (Stream<Path> subpaths = Files.walk(path)) {
      subpaths.forEach(p -> {
        if (Files.isDirectory(p)) {
          addDirectory(p, newDirs);
        } else if (isRudiFile(p)) {
          addFile(p, newDirs);
        }
      });
    }
    return newDirs;
  }


  /** Checks for deleted folders and removes them from the hierarchy */
  public Collection<Path> removeObsolete() {
    List<Path> result = new ArrayList<>(_fileMap.keySet());
    Iterator<Path> it = result.iterator();
    while (it.hasNext()) {
      Path x = it.next();
      if (! Files.exists(x)) {
        removeFromHierarchy(x);
      } else {
        it.remove();
      }
    }
    return result;
  }

  /** Sets all files as in sync with compiled code. */
  public void setFilesUpToDate() {
    resetFilesModifiedProperties();
    _modificationsAfterCompilation.setValue(FILES_SYNCED);
  }

  /** Indicates if there were modifications after the last compilation. */
  public IntegerProperty modificationsAfterCompilationProperty() {
    return _modificationsAfterCompilation;
  }

  /** @return The root <code>TreeItem</code> of the Hierarchy */
  public TreeItem<RudiPath> getRoot() { return _root; }

  /** @return A stream containing all known files and folders. */
  public Stream<RudiPath> getRudiPathSet() {
    return _fileMap.values()
        .stream()
        .map((TreeItem<RudiPath> ti) -> ti.getValue());
  }

}

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
   * TODO
   */
  public RudiHierarchy(Path rudiFolder, Path ruleLocYaml) {
    log.debug("Initializing the RudiFolderHierarchy...");
    _rudiFolder = rudiFolder;
    _ruleLocYaml = ruleLocYaml;
    log.debug("Initialized the RudiFolderHierarchy.");
  }

  /** Resets the rudiHierarchy. */
  public void reset() {
    log.debug("Resetting the RudiFolderHierarchy...");
    _root = null;
    _fileMap.clear();
    _modificationsAfterCompilation.set(FILES_SYNC_NO_PROJECT);
    log.debug("Resetted the RudiFolderHierarchy.");
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

  private void treatParentDir(Path f, TreeItem<RudiPath> ti) {
    Path parentDir = f.getParent();
    addDirectoryToHierarchy(parentDir);
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
  public void addDirectoryToHierarchy(Path f) {
    assert Files.isDirectory(f);
    if (_fileMap.containsKey(f)) return;
    TreeItem<RudiPath> ti = getTreeItem(f);

    if (f.equals(_rudiFolder)) {
      _root = ti;
    } else { // (! f.equals(_rudiFolder))
      /* link to the parent folder's TreeItem */
      treatParentDir(f, ti);
    }
  }

  /**
   * Adds a newly appeared file to the rudiHierarchy.
   *
   * @param f
   *        A <code>.rudi</code> file
   */
  public void addFileToHierarchy(Path f) {
    assert ! Files.isDirectory(f);
    if (_fileMap.containsKey(f)) return;
    TreeItem<RudiPath> ti = getTreeItem(f);

    /* Check if modified since last compilation */
    if (Files.exists(_ruleLocYaml)) {
      if (f.toFile().lastModified() > _ruleLocYaml.toFile().lastModified()) {
        ti.getValue().modifiedProperty().set(true);
      }
    }

    treatParentDir(f, ti);
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
      log.warn("Trying to remove not added file {}", f);
    }
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

  /** Sets a file as modified since last compilation. */
  public void setFileAsModified(Path file) {
    _fileMap.get(file).getValue().modifiedProperty().setValue(true);
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
    _fileMap.values().forEach((ti) -> {
      ti.getValue().modifiedProperty().setValue(false);
    });
  }

  /** @return True, if file is already known, else false */
  public boolean isFileInHierarchy(Path file) {
    return _fileMap.containsKey(file);
  }


  /* ***************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS AND PROPERTIES
   * **************************************************************************/

  /** Indicates if there were modifications after the last compilation. */
  public IntegerProperty modificationsAfterCompilationProperty() {
    return _modificationsAfterCompilation;
  }

  /** @return The root <code>TreeItem</code> of the Hierarchy */
  public TreeItem<RudiPath> getRoot() { return _root; }

  /** @return A map containing all known files and folders.
   *
   *  TODO: FIX THIS */
  public Stream<RudiPath> getRudiPathSet() {
    return _fileMap.values()
        .stream()
        .map((TreeItem<RudiPath> ti) -> ti.getValue());
  }

}

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

package de.dfki.mlt.rudibugger.project.watchServices;
import static de.dfki.mlt.rudimant.common.Constants.*;

import de.dfki.mlt.rudibugger.view.fileTreeView.RudiHierarchy;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Watch's purpose is to check the folders containing <code>.rudi</code>
 * files for changes. If there are changes, a function to refresh the DataModel
 * is called.
 *
 * TODO: only needs RudiHierarchy
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiFolderWatch {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("rudiFolWatch");


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** Represents the Thread in which the WatchService is run. */
  private volatile Thread watchingTread;

  /** The corresponding WatchService */
  private final WatchService _watchService;

  /** Contains all .rudi files. */
  private final Path _rudiFolder;

  /** Represents the hierarchy of all .rudi files. */
  private final RudiHierarchy _rudiHierarchy;

  /** Maps paths to the WatchKey created when monitoring the path with watch service **/
  private final Map<Path, WatchKey> _path2WatchKeyMap;

  /* ***************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   * **************************************************************************/

  /** Private nullary construct to obstruct instantiating. */
  private RudiFolderWatch(Path rudiFolder, RudiHierarchy rudiHierarchy,
          WatchService watchService) {
    _rudiHierarchy = rudiHierarchy;
    _watchService = watchService;
    _rudiFolder = rudiFolder;
    _path2WatchKeyMap = new HashMap<>();
  }

  /**
   * Creates the WatchService to check for changes in the <code>.rudi</code>
   * folder.
   *
   * @param model The current <code>DataModel</code>.
   * @return The created WatchService
   */
  public static RudiFolderWatch createRudiFolderWatch(
          RudiHierarchy rudiHierarchy, Path rudiFolder) {
    RudiFolderWatch newWatch = null;
    try {
      WatchService watchService = FileSystems.getDefault().newWatchService();

      newWatch = new RudiFolderWatch(rudiFolder, rudiHierarchy, watchService);
      recursivelyRegisterPath(rudiFolder, newWatch);
      newWatch.startListening();
    } catch (IOException e) {
      log.error("Could not register WatchService: " + e);
      return null;
    }
    return newWatch;
  }

  private static void recursivelyRegisterPath(Path path, RudiFolderWatch rfw) throws IOException {
    WatchKey key = path.register(rfw._watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
    rfw._path2WatchKeyMap.put(path, key);
    if (!rfw._rudiHierarchy.isFileInHierarchy(path)) {
      rfw._rudiHierarchy.addFileToHierarchy(path);
    }
    try (Stream<Path> subpaths = Files.walk(path)) {
      subpaths.forEach(x -> {
        try {
          if (Files.isDirectory(x)) {
            WatchKey xKey = x.register(rfw._watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
            rfw._path2WatchKeyMap.put(x, xKey);
            if (!rfw._rudiHierarchy.isFileInHierarchy(x)) {
              rfw._rudiHierarchy.addFileToHierarchy(x);
            }
          } else if (x.getFileName().toString().endsWith(RULE_FILE_EXTENSION)
              && !rfw._rudiHierarchy.isFileInHierarchy(x)) {
            rfw._rudiHierarchy.addFileToHierarchy(x);
          }
        } catch (IOException ex) {
          log.error("Could not set watch for subdirectories: " + ex);
        }
      });
    }
  }

  private static void unregisterStaleWatches(RudiFolderWatch rfw) {
    Set<Path> paths = new HashSet<Path>(rfw._path2WatchKeyMap.keySet());
    Set<Path> stalePaths = new HashSet<Path>();
    for (Path path : paths) {
      if (!Files.exists(path)) {
        stalePaths.add(path);
      }
    }
    for (Path stalePath : stalePaths) {
      WatchKey staleWatchKey = rfw._path2WatchKeyMap.get(stalePath);
      if (staleWatchKey != null) {
        log.debug("remove stale watch for " + stalePath);
          staleWatchKey.cancel();
          rfw._path2WatchKeyMap.remove(stalePath);
      }
    }
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /** Starts listening for folder changes. */
  private void startListening() {
    watchingTread = new Thread() {
      @Override
      public void run() {
        try {
          eventLoop();
        } catch (IOException | InterruptedException ex) {
          log.error(ex.getMessage());
          watchingTread = null;
        }
      }
    };
    watchingTread.setDaemon(true);
    watchingTread.setName("rudiFolderWatchingTread");
    watchingTread.start();
    log.info("RudiFolderWatch has been started.");
  }

  /** Stops listening for folder changes. */
  public void shutDownListener() {
    Thread thr = watchingTread;
    if (thr != null) {
      thr.interrupt();
    }
    try {
      _watchService.close();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void eventLoop() throws IOException, InterruptedException {

    WatchKey rudiKey;
    while ((rudiKey = _watchService.take()) != null) {
      for (WatchEvent<?> event : rudiKey.pollEvents()) {
        Path eventPath = ((Path)rudiKey.watchable()).resolve(((WatchEvent<Path>) event).context());
        if (event.kind() == ENTRY_DELETE) {
          if (_path2WatchKeyMap.containsKey(eventPath)) {
            // only folders have keys, so a folder was deleted
            unregisterStaleWatches(this);
            _rudiHierarchy.removeObsoleteFolders();
          } else if (eventPath.getFileName().toString().endsWith(RULE_FILE_EXTENSION)) {
            // rudi file was deleted
            Platform.runLater(
                () -> {
                  _rudiHierarchy.removeFromFileHierarchy(eventPath);
                  log.info("rudi file deleted: " + eventPath);
                });
          }
        } else if (event.kind() == ENTRY_CREATE) {
          if (Files.isDirectory(eventPath)) {
            // folder created
            recursivelyRegisterPath(eventPath, this);
            log.debug("Started watching new folder: " + eventPath);
          } else if (eventPath.getFileName().toString().endsWith(RULE_FILE_EXTENSION)
              && ! Files.isHidden(eventPath)) {
            // rudi file created
            Platform.runLater(
                () -> {
                  if (_rudiHierarchy.isFileInHierarchy(eventPath)) {
                    log.info("rudi file has been modified : " + eventPath);
                  } else {
                    _rudiHierarchy.addFileToHierarchy(eventPath);
                    log.info("rudi file added: " + eventPath);
                  }
                  _rudiHierarchy.setFileAsModified(eventPath);
                });
          }
        } else if (event.kind() == ENTRY_MODIFY && Files.exists(eventPath)
            && !Files.isDirectory(eventPath)
            && eventPath.getFileName().toString().endsWith(RULE_FILE_EXTENSION)) {
          // rudi file modified, we don't care about modified folders
          Platform.runLater(
              () -> {
                log.info("rudi file has been modified : " + eventPath);
                _rudiHierarchy.setFileAsModified(eventPath);
              });
        }
       }
      rudiKey.reset();
    }
  }
}

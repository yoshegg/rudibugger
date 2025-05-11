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
import static de.dfki.mlt.rudimant.common.Constants.RULE_FILE_EXTENSION;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.mlt.rudibugger.view.fileTreeView.RudiHierarchy;
import javafx.application.Platform;

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

  /** Represents the hierarchy of all relevant files and directories (under the
   *  root given by the config). */
  private final RudiHierarchy _rudiHierarchy;

  /** Maps paths to the WatchKey created when monitoring the path with watch service **/
  private final Map<Path, WatchKey> _path2WatchKeyMap;

  /* ***************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   * **************************************************************************/

  /** Private nullary construct to obstruct instantiating. */
  private RudiFolderWatch(RudiHierarchy rudiHierarchy,
          WatchService watchService) {
    _rudiHierarchy = rudiHierarchy;
    _watchService = watchService;
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

      newWatch = new RudiFolderWatch(rudiHierarchy, watchService);
      newWatch.recursivelyRegisterPath(rudiFolder);
      newWatch.startListening();
    } catch (IOException e) {
      log.error("Could not register WatchService: " + e);
      return null;
    }
    return newWatch;
  }

  private static boolean isRudiFile(Path p) {
    try {
      return ! Files.isHidden(p) &&
        p.getFileName().toString().endsWith(RULE_FILE_EXTENSION);
    }
    catch (Exception ex) {
      log.error("Unexpected File Exception: {}", ex);
    }
    return false;
  }

  private void recursivelyRegisterPath(Path path) throws IOException {
    try (Stream<Path> subpaths = Files.walk(path)) {
      subpaths.forEach(p -> {
        if (Files.isDirectory(p)) {
          try {
            WatchKey key = p.register(_watchService, ENTRY_MODIFY,
                ENTRY_CREATE, ENTRY_DELETE);
            _path2WatchKeyMap.put(p, key);
            _rudiHierarchy.addDirectoryToHierarchy(p);
            log.info("Watching directory {}", p);
          } catch (IOException ex) {
            log.error("Could not set watch for subdirectory: " + ex);
          }
        } else if (isRudiFile(p)) {
          _rudiHierarchy.addFileToHierarchy(p);
        }
      });
    }
  }

  private void unregisterStaleWatches(Collection<Path> stalePaths) {
    for (Path stalePath : stalePaths) {
      WatchKey staleWatchKey = _path2WatchKeyMap.get(stalePath);
      if (staleWatchKey != null) {
        log.debug("remove stale watch for " + stalePath);
        staleWatchKey.cancel();
        _path2WatchKeyMap.remove(stalePath);
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

  private void fileAddedOrModified(Path p) {
    if (_rudiHierarchy.isFileInHierarchy(p)) {
      log.info("rudi file has been modified: {}", p);
    } else {
      _rudiHierarchy.addFileToHierarchy(p);
      log.info("rudi file added: {}", p);
    }
    _rudiHierarchy.setFileAsModified(p);
  }

  public void eventLoop() throws IOException, InterruptedException {

    WatchKey rudiKey;
    while ((rudiKey = _watchService.take()) != null) {
      for (WatchEvent<?> event : rudiKey.pollEvents()) {
        Path eventPath = ((Path)rudiKey.watchable()).resolve(((WatchEvent<Path>) event).context());
        if (event.kind() == ENTRY_DELETE) {
          // rudi file was deleted
          Platform.runLater(() -> {
            unregisterStaleWatches(_rudiHierarchy.removeObsolete());
          });

        } else if (event.kind() == ENTRY_CREATE) {
          if (Files.isDirectory(eventPath)) {
            // folder created
            recursivelyRegisterPath(eventPath);
            log.debug("Started watching new folder: " + eventPath);
          } else if (isRudiFile(eventPath)) {
            // rudi file created
            Platform.runLater(() -> fileAddedOrModified(eventPath));
          }
        } else if (event.kind() == ENTRY_MODIFY && Files.exists(eventPath)
            && ! Files.isDirectory(eventPath) && isRudiFile(eventPath)) {
          // rudi file modified, we don't care about modified folders
          Platform.runLater(() -> fileAddedOrModified(eventPath));
        }
       }
      rudiKey.reset();
    }
  }
}

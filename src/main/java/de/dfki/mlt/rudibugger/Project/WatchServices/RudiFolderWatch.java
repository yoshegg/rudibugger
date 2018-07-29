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

package de.dfki.mlt.rudibugger.Project.WatchServices;
import static de.dfki.mlt.rudimant.common.Constants.*;

import de.dfki.mlt.rudibugger.FileTreeView.RudiHierarchy;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
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


  /*****************************************************************************
   * FIELDS
   ****************************************************************************/

  /** Represents the Thread in which the WatchService is run. */
  private volatile Thread watchingTread;

  /** The corresponding WatchService */
  private final WatchService _watchService;

  /** Contains all .rudi files. */
  private final Path _rudiFolder;

  /** Represents the hierarchy of all .rudi files. */
  private final RudiHierarchy _rudiHierarchy;


  /*****************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   ****************************************************************************/

  /** Private nullary construct to obstruct instantiating. */
  private RudiFolderWatch(Path rudiFolder, RudiHierarchy rudiHierarchy,
          WatchService watchService) {
    _rudiHierarchy = rudiHierarchy;
    _watchService = watchService;
    _rudiFolder = rudiFolder;
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

    WatchService watchService;
    try {
      watchService = FileSystems.getDefault().newWatchService();
      rudiFolder.register(watchService, ENTRY_MODIFY, ENTRY_CREATE,
              ENTRY_DELETE);

      /* iterate over all subdirectories */
      Stream<Path> subpaths = Files.walk(rudiFolder);
      subpaths.forEach(x -> {
        try {
          if (Files.isDirectory(x))
            x.register(watchService, ENTRY_MODIFY, ENTRY_CREATE,
                    ENTRY_DELETE);
        } catch (IOException ex) {
          log.error("Could not set watch for subdirectories: " + ex);
        }
      });
    } catch (IOException e) {
      log.error("Could not register WatchService: " + e);
      return null;
    }

    RudiFolderWatch newWatch
            = new RudiFolderWatch(rudiFolder, rudiHierarchy, watchService);
    newWatch.startListening();
    return newWatch;
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Starts listening for folder changes. */
  private void startListening() {
    watchingTread = new Thread() {
      @Override
      public void run() {
        try {
          eventLoop();
        } catch (IOException | InterruptedException ex) {
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
  }

  public void eventLoop() throws IOException, InterruptedException {

    for (;;) {

      WatchKey rudiKey;

      /* watch for changes in the rudi folder */
      try {
        rudiKey = _watchService.take();
      } catch (InterruptedException x) {
        return;
      }

      for (WatchEvent<?> event : rudiKey.pollEvents()) {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == OVERFLOW) {
          log.error("An overflow while checking the rudi "
                  + "folder occured.");
          continue;
        }

        WatchEvent<Path> ev = (WatchEvent<Path>) event;
        Path folder = (Path)rudiKey.watchable();
        Path file = folder.resolve(ev.context());

        /* are folder's containing files changing? */
        if ((kind == ENTRY_CREATE || kind == ENTRY_DELETE
                || kind == ENTRY_MODIFY)
                && file.getFileName().toString()
                        .endsWith(RULE_FILE_EXTENSION)
                && ! Files.isHidden(file)
                ) {

          /* rudi file added */
          if (kind == ENTRY_CREATE) {
            Platform.runLater(() -> {
              if (_rudiHierarchy.isFileInHierarchy(file)) {
                log.info("rudi file has been modified : " + file);
              } else {
                _rudiHierarchy.addFileToHierarchy(file);
                log.info("rudi file added: " + file);
              }
              _rudiHierarchy.setFileAsModified(file);
            });

          }

          /* rudi file modified */
          if (kind == ENTRY_MODIFY) {
            Platform.runLater(() -> {
              log.info("rudi file has been modified : " + file);
              _rudiHierarchy.setFileAsModified(file);
            });
          }

          /* rudi file deleted */
          if (kind == ENTRY_DELETE) {
            Platform.runLater(() -> {
              _rudiHierarchy.removeFromFileHierarchy(file);
              log.info("rudi file deleted: " + file);
            });
          }

          continue;
        }

        /* new folder created? */
        if ((kind == ENTRY_CREATE || kind == ENTRY_DELETE
                || kind == ENTRY_MODIFY) && Files.isDirectory(file)) {

          /* watch another folder */
          if (kind == ENTRY_CREATE) {
            file.register(_watchService, ENTRY_MODIFY, ENTRY_CREATE,
                  ENTRY_DELETE);
            _rudiHierarchy.addFileToHierarchy(file);
            log.debug("Started watching new folder: " + file);
          }
        }
      }


      /* if the watchKey is no longer valid, leave the eventLoop */
      if (rudiKey != null) {
        boolean valid = rudiKey.reset();
        if (!valid) {
          log.debug("watchKey no longer valid. Probably because a watched folder has been deleted");
          log.debug("Restarting RudiFolderWatch...");
          createRudiFolderWatch(_rudiHierarchy, _rudiFolder);
          _rudiHierarchy.removeObsoleteFolders();
          break;
        }
      }
    }
  }
}

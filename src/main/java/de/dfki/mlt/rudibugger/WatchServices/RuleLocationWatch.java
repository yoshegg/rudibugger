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

package de.dfki.mlt.rudibugger.WatchServices;
import static de.dfki.mlt.rudimant.common.Constants.*;

import de.dfki.mlt.rudibugger.DataModel;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Watch's purpose is to check if the <code>RuleLocation.yml</code> file
 * is being changed. If it is, a function to refresh the DataModel is called.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleLocationWatch {

  private RuleLocationWatch() {}

  static Logger log = LoggerFactory.getLogger("ruleLocWatch");

  /** the Thread in which the WatchService is run */
  private volatile Thread watchingTread;

  /** start listening for file changes */
  private void startListening() {
    watchingTread = new Thread() {
      @Override
      public void run() {
        try {
          eventLoop();
        } catch (IOException|InterruptedException ex) {
          watchingTread = null;
        }
      }
    };
    watchingTread.setDaemon(true);
    watchingTread.setName("ruleLocWatchingTread");
    watchingTread.start();
    log.info("RuleLocationWatch has been started.");
  }

  /** stop listening for file changes */
  public void shutDownListener() {
    Thread thr = watchingTread;
    if (thr != null) {
      thr.interrupt();
    }
  }

  /** the corresponding WatchSercie */
  private WatchService _watchService;

  /** this Path contains the currently modified file */
  private Path changingFile;

  /** the DataModel */
  DataModel _model;

  /**
   * this function must be called to create a WatchService for ~RuleLocation.yml
   * @param model
   * @return
   */
  public static RuleLocationWatch createRuleLocationWatch(DataModel model) {
    RuleLocationWatch newWatch = new RuleLocationWatch();
    newWatch._model = model;

    try {
      newWatch._watchService = FileSystems.getDefault().newWatchService();
      newWatch._model.project.getGeneratedDirectory()
        .register(newWatch._watchService, ENTRY_MODIFY, ENTRY_CREATE);
      log.debug("registered " + model.project.getGeneratedDirectory());
    } catch (IOException e) {
      log.error("Could not register WatchService: " + e);
    }
    newWatch.startListening();
    return newWatch;
  }

  public void eventLoop() throws IOException, InterruptedException {

    /* this is necessary to avoid take() and be able to use poll() */
    boolean ruleLocationFileChanged = false;

    for (;;) {

      WatchKey watchKey;

      /* if no file change has been detected */
      if (! ruleLocationFileChanged) {
        try {
          watchKey = _watchService.take();
        } catch (InterruptedException ex) {
          log.error(ex.toString());
          return;
        }

        /* identify what has happened */
        for (WatchEvent<?> event : watchKey.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();

          if (kind == OVERFLOW) {
            log.error("An overflow while checking RuleLoc.yml's "
                    + "folder occured.");
            continue;
          }

          WatchEvent<Path> ev = (WatchEvent<Path>) event;
          changingFile = ev.context();

          /* is RuleLoc.yml changing? */
          if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY)
                  && changingFile.getFileName().toString()
                              .equals(RULE_LOCATION_FILE)) {
            ruleLocationFileChanged = true;
            log.debug("[" + changingFile + "] is being modified / created.");
          }
        }
      }

      /* if a file change has been detected before */
      else {
        /* loop until no more change is detected in the root folder */
        while (true) {
          /* this is null, if no more change is going on */
          watchKey = _watchService.poll(500, TimeUnit.MILLISECONDS);

          try {
            /* remove the events or watchKey can't be resetted properly */
            for (WatchEvent<?> event : watchKey.pollEvents()) {
              WatchEvent.Kind<?> kind = event.kind();

              if (kind == OVERFLOW) {
                continue;
              }

              WatchEvent<Path> ev = (WatchEvent<Path>) event;
              changingFile = ev.context();

              /* is ~RuleLocation.yml changing? */
              if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY)
                      && changingFile.getFileName().toString()
                              .equals(RULE_LOCATION_FILE)) {
                log.debug("[" + changingFile + "] is still being modified.");
              }
              /* is some other file changing? */
              else {
                log.warn("A file has been changed during modification of the"
                        + "Rule Location file: " + changingFile);
              }
            }
            watchKey.reset();
            continue;
          } catch (NullPointerException ex) {
            log.debug("[" + changingFile + "] is ready.");
          }

          /* no more changes are detected, update the project */
          if (watchKey == null) {
            Platform.runLater(() -> {
              log.debug("[" + changingFile + "] has changed.");
              if (_model.ruleModel.getRootImport() == null) {
                _model.ruleModel.init();
              } else {
                _model.ruleModel.update();
              }
              _model.rudiHierarchy.setFilesUpToDate();
            });
            ruleLocationFileChanged = false;
            break;
          }
        }
      }

      /* if the watchKey is no longer valid, leave the eventLoop */
      if (watchKey != null) {
        boolean valid = watchKey.reset();
        if (!valid) {
          break;
        }
      }
    }
  }
}

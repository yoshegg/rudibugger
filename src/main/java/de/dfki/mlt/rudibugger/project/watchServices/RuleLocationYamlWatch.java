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
import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudibugger.project.ruleModel.RuleModel;
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
 * TODO: only needs RuleModel (and RudiHierachy?)
 * updating the RuleModel could also update the hierarchy internally
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleLocationYamlWatch {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("ruleLocWatch");


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** Represents the Thread in which the WatchService is run. */
  private volatile Thread watchingTread;

  /** The corresponding WatchService. */
  private WatchService _watchService;

  /** This Path contains the currently modified file. */
  private Path changingFile;

//  /** Represents the rule structure of the observed project. */
//  private final RuleModel _ruleModel;

  /** TODO */
  private final Project _project;

  /** Represents the hierarchy of all .rudi files. */
  private final RudiHierarchy _rudiHierarchy;


  /* ***************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   * **************************************************************************/

  private RuleLocationYamlWatch(Project project,
          RudiHierarchy rudiHierarchy, WatchService watchService) {
//    _ruleModel = ruleModel;
    _rudiHierarchy = rudiHierarchy;
    _watchService = watchService;
    _project = project;
  }

  /**
   * Creates the Watch to check for changes of the file
   * <code>RuleLoc.yml</code>.
   *
   * TODO
   * @return The created WatchService
   */
  public static RuleLocationYamlWatch createRuleLocationWatch(
          Project project, RudiHierarchy rudiHierarchy,
          Path generatedFilesFolder) {

    WatchService watchService;
    try {
      watchService = FileSystems.getDefault().newWatchService();
      generatedFilesFolder.register(watchService, ENTRY_MODIFY, ENTRY_CREATE);
      log.debug("registered " + generatedFilesFolder);
    } catch (IOException e) {
      log.error("Could not register WatchService: " + e);
      return null;
    }

    RuleLocationYamlWatch newWatch
        = new RuleLocationYamlWatch(project, rudiHierarchy, watchService);
    newWatch.startListening();
    return newWatch;
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  /** Starts listening for <code>RuleLoc.yml</code> changes .*/
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

  /** Stops listening for <code>RuleLoc.yml</code> changes. */
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
        watchKey.reset();
      }

      /* if a file change has been detected before */
      else {
        /* loop until no more change is detected in the root folder */
        while (true) {
          /* this is null, if no more change is going on */
          watchKey = _watchService.poll(500, TimeUnit.MILLISECONDS);

          /* if no more changes are detected, update the project */
          if (watchKey == null) {
            Platform.runLater(() -> {
              log.debug("[" + changingFile + "] has changed.");
              _project.initRuleModel();
//              if (_ruleModel.getRootImport() == null) {
//                _ruleModel.init();
//              } else {
//                _ruleModel.update();
//              }
              _rudiHierarchy.setFilesUpToDate();
            });
            ruleLocationFileChanged = false;
            break;
          }

          /* remove the events or watchKey can't be resetted properly */
          for (WatchEvent<?> event : watchKey.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();
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
        }
      }
    }
  }
}

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.WatchServices;

import static de.dfki.rudibugger.Constants.*;
import de.dfki.rudibugger.DataModel;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.apache.log4j.Logger;

/**
 * This watch's one and only purpose is to check if the RuleLocation.yml file
 * is being changed. If it is, a function to refresh the DataModel is called.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleLocationWatch {

  static Logger log = Logger.getLogger("ruleLocWatch");

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
  private void shutDownListener() {
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
   */
  public void createRuleLocationWatch(DataModel model) {
    _model = model;

    try {
      _watchService = FileSystems.getDefault().newWatchService();
      model.getRootFolder().register(_watchService, ENTRY_MODIFY, ENTRY_CREATE);
    } catch (IOException e) {
      log.error("Could not register WatchService: " + e);
    }
    startListening();
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
          log.error(ex);
          return;
        }

        /* identify what has happened */
        for (WatchEvent<?> event : watchKey.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();

          if (kind == OVERFLOW) {
            log.error("An overflow while checking RuleLocation.yml's "
                    + "folder occured.");
            continue;
          }

          WatchEvent<Path> ev = (WatchEvent<Path>) event;
          changingFile = ev.context();

          /* is ~RuleLocation.yml changing? */
          if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY)
                  && changingFile.getFileName().toString()
                          .endsWith(RULE_LOCATION_SUFFIX)) {
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
                              .endsWith(RULE_LOCATION_SUFFIX)) {
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

          /* no more changes are detected, notify DataModel to reload */
          if (watchKey == null) {
            Platform.runLater(() -> {
              log.debug("[" + changingFile + "] has changed.");
              _model.setRuleModelChangeStatus(RULE_MODEL_CHANGED);
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

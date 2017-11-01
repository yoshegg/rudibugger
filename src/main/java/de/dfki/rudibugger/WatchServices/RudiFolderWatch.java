/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.WatchServices;

import de.dfki.rudibugger.DataModel;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javafx.application.Platform;
import org.apache.log4j.Logger;

/**
 * This watch's one and only purpose is to check the folder containing .rudi
 * files for changes. If there are changes, a function to refresh the DataModel
 * is called.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiFolderWatch {

  static Logger log = Logger.getLogger("rudiFolWatch");

  /**
   * the Thread in which the WatchService is run
   */
  private volatile Thread watchingTread;

  /**
   * start listening for folder changes
   */
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

  /**
   * stop listening for folder changes
   */
  public void shutDownListener() {
    Thread thr = watchingTread;
    if (thr != null) {
      thr.interrupt();
    }
  }

  /**
   * the corresponding WatchSercie
   */
  private WatchService _watchService;

  /**
   * the DataModel
   */
  DataModel _model;

  public void createRudiFolderWatch(DataModel model) {
    _model = model;

    try {
      _watchService = FileSystems.getDefault().newWatchService();
      model.getRudiFolder().register(_watchService, ENTRY_MODIFY, ENTRY_CREATE,
              ENTRY_DELETE);
    } catch (IOException e) {
      log.error("Could not register WatchService: " + e);
    }
    startListening();
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
        Path filename = _model.getRudiFolder().resolve(ev.context());


        /* is rudi folder changing? */
        if ((kind == ENTRY_CREATE || kind == ENTRY_DELETE)
                && filename.getFileName().toString().endsWith(".rudi")) {

          /* rudi file added */
          if (kind == ENTRY_CREATE) {

            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                if (_model.addRudiPath(filename))
                  log.info("rudi file added: " + filename);
              }
            });

          }

          /* rudi file modified */
          if (kind == ENTRY_MODIFY) {
            // TODO
            log.info("rudi file has been modified: " + filename);
          }

          /* rudi file deleted */
          if (kind == ENTRY_DELETE) {

            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                _model.removeRudiPath(filename);
                log.info("rudi file deleted: " + filename);
              }
            });

          }
        }
      }


      /* if the watchKey is no longer valid, leave the eventLoop */
      if (rudiKey != null) {
        boolean valid = rudiKey.reset();
        if (!valid) {
          break;
        }
      }
    }
  }
}

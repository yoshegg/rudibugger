/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.WatchServices;

import de.dfki.rudibugger.project.Project;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javafx.application.Platform;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Watch {

  static Logger log = Logger.getLogger("rudiLog");

  // the Project
  private volatile Project _project;

  // the Thread
  private volatile Thread watchingTread;

  public void startListening() {
    log.info("Watch has been started.");
    watchingTread = new Thread() {
      public void run() {
        try {
          eventLoop();
        } catch (IOException|InterruptedException ex) {
          watchingTread = null;
        }
      }
    };
    watchingTread.setDaemon(true);
    watchingTread.setName("watchingTread");
    watchingTread.start();
  }

  public void shutDownListener() {
    Thread thr = watchingTread;
    if (thr != null) {
      thr.interrupt();
    }
  }

  private WatchService _watcherRootFolder;
  private WatchService _watcherRudisFolder;


  public void createProjectWatch(Project proj) {
    _project = proj;
    Path rootFolder = _project.getRootFolder();
    Path rudisFolder = _project.getRudisFolder();
    try {
      _watcherRootFolder = FileSystems.getDefault().newWatchService();
      _watcherRudisFolder = FileSystems.getDefault().newWatchService();
      rootFolder.register(_watcherRootFolder, ENTRY_MODIFY);
      rudisFolder.register(_watcherRudisFolder, ENTRY_MODIFY);
    } catch (IOException e) {
      log.error("Could not register WatchService: " + e);
    }
    startListening();
  }

  public void createDirectoryWatch(Project proj) {
    _project = proj;
    Path rootFolder = _project.getRootFolder();
    try {
      _watcherRootFolder = FileSystems.getDefault().newWatchService();
      rootFolder.register(_watcherRootFolder, ENTRY_MODIFY);
    } catch (IOException e) {
      log.error("Could not register WatchService: " + e);
    }
    startListening();
  }

  public void eventLoop() throws IOException, InterruptedException {
    for (;;) {

      // watch for changes in the root folder
      WatchKey rootKey;
      try {
        rootKey = _watcherRootFolder.take();
      } catch (InterruptedException x) {
        return;
      }

      for (WatchEvent<?> event : rootKey.pollEvents()) {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == OVERFLOW) {
          continue;
        }

        WatchEvent<Path> ev = (WatchEvent<Path>) event;
        Path filename = ev.context();

        log.debug("Root folder change: " + filename);
        // wait for the file being finished
        Thread.sleep(500);
        _project.retrieveLocRuleTreeView();
        Platform.runLater(() -> {
          try {
            _project.retrieveRuleLocMap();
          } catch (FileNotFoundException ex) {
            log.error(ex);
          }
        });
      }

      rootKey.reset();

      // watch for changes in the rudi folder
      if (_watcherRudisFolder != null) {
        WatchKey rudiKey;
        try {
          rudiKey = _watcherRudisFolder.take();
        } catch (InterruptedException x) {
          return;
        }

        for (WatchEvent<?> event : rootKey.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();

          if (kind == OVERFLOW) {
            continue;
          }

          WatchEvent<Path> ev = (WatchEvent<Path>) event;
          Path filename = ev.context();

          System.out.println(filename);
        }

        rudiKey.reset();
      }
    }
  }
}

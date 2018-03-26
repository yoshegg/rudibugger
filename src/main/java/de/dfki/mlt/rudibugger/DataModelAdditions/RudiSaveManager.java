/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.DataModelAdditions;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.TabManagement.RudiTab;
import static de.dfki.mlt.rudimant.common.Constants.RULE_FILE_EXTENSION;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.animation.PauseTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality to save .rudi files.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiSaveManager {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("rudiSave");

  /** The <code>DataModel</code> */
  private final DataModel _model;

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public RudiSaveManager(DataModel model) { _model = model; }

  /**
   * Saves a file by overwriting the old version without asking.
   */
  public void quickSaveFile() {
    RudiTab tab = _model.selectedTabProperty().get();
    Path file = tab.getFile();
    String content = tab.getRudiCode();

    if (saveFile(file, content)) {
      tab.setText(file.getFileName().toString());
      tab.waitForModif();
      log.debug("File " + file.getFileName() + " has been saved.");
      notifySaved(file.getFileName().toString());
    }
  }

  /**
   * Saves a given String into a given file.
   *
   * @param file the path of the to-be-saved file
   * @param content the content of the to-be-saved file
   * @return True, if the file has been successfully saved, else false
   */
  private boolean saveFile(Path file, String content) {
    try {
      Files.write(file, content.getBytes());
      return true;
    } catch (IOException e) {
      log.error("Could not save " + file);
      return false;
    }
  }

  /**
   * Quick-save all open files.
   */
  public void quickSaveAllFiles() {
    for (RudiTab tab : _model.openTabsProperty().getValue().values()) {
      Path file = tab.getFile();
      String content = tab.getRudiCode();
      if (tab.hasBeenModifiedProperty().getValue()) {
        if (saveFile(file, content)) {
          tab.setText(file.getFileName().toString());
          tab.waitForModif();
          log.debug("File " + file.getFileName() + " has been saved.");
          notifySaved(file.getFileName().toString());
        }
      }
    }
  }

  /**
   * Save tab's content into a new file.
   */
  public void saveFileAs() {
    RudiTab tab = _model.selectedTabProperty().get();
    String content = tab.getRudiCode();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(_model.project.getRudiFolder().toFile());
    FileChooser.ExtensionFilter extFilter
            = new FileChooser.ExtensionFilter
        ("rudi file (*" + RULE_FILE_EXTENSION + ")", "*" + RULE_FILE_EXTENSION);
    fileChooser.getExtensionFilters().add(extFilter);
    Path file;
    try {
      file = (fileChooser.showSaveDialog(_model.stageX)).toPath();
    } catch (NullPointerException e) {
      return;
    }
      if (! file.getFileName().toString().endsWith(RULE_FILE_EXTENSION)) {
        file = Paths.get(file.toString() + RULE_FILE_EXTENSION);
    }

    if (saveFile(file, content)) {

      /* close old tab */
      EventHandler<Event> handler = tab.getOnClosed();
      if (null != handler) {
        handler.handle(null);
      } else {
        _model.requestedCloseTabProperty().setValue(tab);
        tab.getTabPane().getTabs().remove(tab);
      }

      /* open a new tab */
      _model.rudiLoad.openFile(file);

      log.debug("File " + file.getFileName() + " has been saved.");
    }
  }

  /**
   * Temporarily shows a message on the statusBar.
   *
   * @param file the file that has been saved
   */
  private void notifySaved(String file) {
    _model.statusBarProperty().set("Saved " + file + ".");
    PauseTransition pause = new PauseTransition(Duration.seconds(3));
    pause.setOnFinished(Ce -> _model.statusBarProperty().set(null));
    pause.play();
  }
}

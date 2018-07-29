///*
// * The Creative Commons CC-BY-NC 4.0 License
// *
// * http://creativecommons.org/licenses/by-nc/4.0/legalcode
// *
// * Creative Commons (CC) by DFKI GmbH
// *  - Bernd Kiefer <kiefer@dfki.de>
// *  - Anna Welker <anna.welker@dfki.de>
// *  - Christophe Biwer <christophe.biwer@dfki.de>
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// * IN THE SOFTWARE.
// */
//
//package de.dfki.mlt.rudibugger.DataModelAdditions;
//
//import de.dfki.mlt.rudibugger.DataModel;
//import de.dfki.mlt.rudibugger.TabManagement.RudiTab;
//import static de.dfki.mlt.rudimant.common.Constants.RULE_FILE_EXTENSION;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import javafx.animation.PauseTransition;
//import javafx.stage.FileChooser;
//import javafx.util.Duration;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Provides additional functionality to save .rudi files.
// *
// * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
// */
//public class RudiSaveManager {
//
//  /** The logger. */
//  static Logger log = LoggerFactory.getLogger("rudiSave");
//
//  /** The <code>DataModel</code> */
//  private final DataModel _model;
//
//  /**
//   * Initializes this addition of <code>DataModel</code>.
//   *
//   * @param model  The current <code>DataModel</code>
//   */
//  public RudiSaveManager(DataModel model) {
//    _model = model;
//  }
//
//
//  /** Responsible for processing save requests. */
//  public void initSaveListener() {
//    _model.tabStore.requestedSavingOfTabProperty().addListener((o, ov, nv) -> {
//      if (nv != null) {
//        if (nv.isKnown())
//          quickSaveFile(nv);
//        else
//          saveFileAs(nv);
//        _model.tabStore.requestedSavingOfTabProperty().set(null);
//      }
//    });
//  }
//
////  /** Saves a file by overwriting the old version without asking. */
////  public void quickSaveFile(RudiTab tab) {
////    Path file = tab.getFile();
////    String content = tab.getRudiCode();
////
////    if (saveFile(file, content)) {
////      tab.setText(file.getFileName().toString());
////      tab.waitForModifications();
////      log.debug("File " + file.getFileName() + " has been saved.");
////      notifySaved(file.getFileName().toString());
////    }
////  }
//
//  /**
//   * Saves a given String into a given file.
//   *
//   * @param file the path of the to-be-saved file
//   * @param content the content of the to-be-saved file
//   * @return True, if the file has been successfully saved, else false
//   */
//  private boolean saveFile(Path file, String content) {
//    try {
//      Files.write(file, content.getBytes());
//      return true;
//    } catch (IOException e) {
//      log.error("Could not save " + file);
//      return false;
//    }
//  }
//
//  /** Quick-save all open files. */
//  public void quickSaveAllFiles() {
//    for (RudiTab tab : _model.tabStore.openTabsProperty().getValue().values()) {
//      Path file = tab.getFile();
//      String content = tab.getRudiCode();
//      if (tab.hasBeenModifiedProperty().getValue()) {
//        if (saveFile(file, content)) {
//          tab.setText(file.getFileName().toString());
//          tab.waitForModifications();
//          log.debug("File " + file.getFileName() + " has been saved.");
//          notifySaved(file.getFileName().toString());
//        }
//      }
//    }
//  }
//
//  /**
//   * Save tab's content into a new file.
//   *
//   * @return True, if the file has been successfully saved, else false
//   */
//  public boolean saveFileAs(RudiTab tab) {
//    String content = tab.getRudiCode();
//
//    FileChooser fileChooser = new FileChooser();
//    fileChooser.setInitialDirectory(_model.project.getRudiFolder().toFile());
//    FileChooser.ExtensionFilter extFilter
//            = new FileChooser.ExtensionFilter
//        ("rudi file (*" + RULE_FILE_EXTENSION + ")", "*" + RULE_FILE_EXTENSION);
//    fileChooser.getExtensionFilters().add(extFilter);
//    Path file;
//    try {
//      file = (fileChooser.showSaveDialog(_model.stageX)).toPath();
//    } catch (NullPointerException e) {
//      return false;
//    }
//    if (file == null) return false;
//    if (! file.getFileName().toString().endsWith(RULE_FILE_EXTENSION)) {
//      file = Paths.get(file.toString() + RULE_FILE_EXTENSION);
//    }
//
//    if (saveFile(file, content)) {
//      tab.setText(file.getFileName().toString());
//      _model.tabStore.openTabsProperty().get().remove(tab.getFile());
//      tab.setFile(file);
//      _model.tabStore.openTabsProperty().get().put(file, tab);
//      tab.waitForModifications();
//
//      log.debug("File " + file.getFileName() + " has been saved.");
//      return true;
//    }
//    return false;
//  }
//
//  /**
//   * Temporarily shows a message on the statusBar.
//   *
//   * @param file the file that has been saved
//   */
//  private void notifySaved(String file) {
//    _model.statusBarTextProperty().set("Saved " + file + ".");
//    PauseTransition pause = new PauseTransition(Duration.seconds(3));
//    pause.setOnFinished(Ce -> _model.statusBarTextProperty().set(null));
//    pause.play();
//  }
//}

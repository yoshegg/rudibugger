package de.dfki.mlt.rudibugger.editor;

import de.dfki.mlt.rudibugger.view.editor.RudiTab;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudibuggerEditor extends Editor {

  static Logger log = LoggerFactory.getLogger("rudibuggerEditor");


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  private final Map<Path, RudiTab> _openFiles = new HashMap<>();

  private TabPane _tabPane;

  private ReadOnlyObjectProperty<Tab> _currentlySelectedTab;


  /* ***************************************************************************
   * CONSTRUCTOR & OTHER METHODS
   * **************************************************************************/

  public static RudibuggerEditor getNewEditor() {
    return new RudibuggerEditor();
  }

  private RudibuggerEditor() {}

  public void setTabPane(TabPane tp) {
    _tabPane = tp;
    _currentlySelectedTab = _tabPane.getSelectionModel().selectedItemProperty();
  }

  public static void closeEditor(RudibuggerEditor re) {
    re.closeAllFiles();
  }


  /* ***************************************************************************
   * SAVE METHODS
   * **************************************************************************/

  @Override
  public void saveFile() {
    RudiTab currentTab = (RudiTab) _tabPane.getSelectionModel()
      .getSelectedItem();
    String content = currentTab.getRudiCode();
    Path path = currentTab.getFile();
    writeContentToFileOnDisk(path, content);
    currentTab.waitForModifications();
  };

  public void saveFile(RudiTab rt) {
    String content = rt.getRudiCode();
    Path path = rt.getFile();
    writeContentToFileOnDisk(path, content);
    rt.waitForModifications();
  }

  @Override
  public void saveFileAs(Path path) {
    RudiTab currentTab = (RudiTab) _tabPane.getSelectionModel()
      .getSelectedItem();
    String content = currentTab.getRudiCode();
    writeContentToFileOnDisk(path, content);
    closeFile();
    int oldPosition = currentTab.getCodeArea().getCurrentParagraph() + 1;
    loadFileAtLine(path, oldPosition);
  }

  @Override
  public void saveAllFiles() {
    _openFiles.values().forEach(tab -> {
      if (tab.isModified()) {
        String content = tab.getRudiCode();
        Path path = tab.getFile();
        writeContentToFileOnDisk(path, content);
        tab.waitForModifications();
      }
    });
  };

  /**
   * Writes a given String into a given file.
   *
   * @param file The path of the to-be-saved file
   * @param content The content of the to-be-saved file
   * @return True, if the file has been successfully saved, else false
   */
  public boolean writeContentToFileOnDisk(Path file, String content) {
    try {
      Files.write(file, content.getBytes());
      return true;
    } catch (IOException e) {
      log.error("Could not save " + file);
      return false;
    }
  }


  /* ***************************************************************************
   * LOAD METHODS
   * **************************************************************************/

  @Override
  public void createNewFile() {
    RudiTab emptyTab = new RudiTab("", this);
    _tabPane.getTabs().add(emptyTab);
    _tabPane.getSelectionModel().select(emptyTab);
  }

  @Override
  public void loadFile(Path file) {
    if (! _openFiles.keySet().contains(file)) {
      createAndAddNewTab(file);
      switchToTabAtLine(file, 1);
    } else
      switchToTab(file);
  };

  @Override
  public void loadFileAtLine(Path file, int line) {
    if (! _openFiles.keySet().contains(file))
      createAndAddNewTab(file);
    switchToTabAtLine(file, line);
  };

  private void createAndAddNewTab(Path file) {
    String content = readInFile(file);
    RudiTab newRudiTab = new RudiTab(content, file, this);
    _tabPane.getTabs().add(newRudiTab);
    _openFiles.put(file, newRudiTab);
  }

  private String readInFile(Path file) {
    String content = "";
    try {
      Scanner s = new Scanner(file.toFile()).useDelimiter("\n");
      while (s.hasNext())
        content += (s.next() + "\n");
    } catch (FileNotFoundException e) {
      log.error("Something went wrong while reading in "
        + file.getFileName().toString());
    }
    return content;
  }

  private void switchToTab(Path file) {
    RudiTab wantedTab = _openFiles.get(file);
    _tabPane.getSelectionModel().select(wantedTab);
  }

  private void switchToTabAtLine(Path file, int line) {
    RudiTab wantedTab = _openFiles.get(file);
    wantedTab.getCodeArea().showParagraphPretty(line - 1);
    wantedTab.getCodeArea().moveTo(line - 1, 0);
    _tabPane.getSelectionModel().select(wantedTab);
  }


  /* ***************************************************************************
   * CLOSE METHODS
   * **************************************************************************/

  @Override
  public void closeFile() {
    RudiTab currentTab = (RudiTab) _tabPane.getSelectionModel()
      .getSelectedItem();
    _openFiles.remove(currentTab.getFile());
  };

  public void closeTab(RudiTab rt) {
    _openFiles.remove(rt.getFile());
  }

  @Override
  public void closeAllFiles() {
    _openFiles.values().forEach(x -> removeTabFromTabPane(x));
  };

  private void removeTabFromTabPane(RudiTab rt) {
    Path file = rt.getFile();
    _tabPane.getTabs().remove(rt);
    _openFiles.remove(file);
  }


  /* ***************************************************************************
   * GETTERS
   * **************************************************************************/

  public ReadOnlyObjectProperty<Tab> currentlySelectedTabProperty() {
    return _currentlySelectedTab;
  }

}

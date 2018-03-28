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

package de.dfki.mlt.rudibugger.TabManagement;

import java.nio.file.Path;
import java.util.HashMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for managing tabs:
 * - only 1 tab per file
 * - manage multiple TabPanes
 * - switch to requested tab
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class TabStore {

  /** the logger of the TabStore */
  static Logger log = LoggerFactory.getLogger("TabStore");

  /** the underlying HBox */
  private final HBox tabBox;

  /** all open tabs linked to their path, to verify that none is opened twice */
  private final ObjectProperty<HashMap<Path, RudiTab>> openTabs
          = new SimpleObjectProperty<>(new HashMap<>());
  public ObjectProperty<HashMap<Path, RudiTab>> openTabsProperty() {
    return openTabs;
  }

  /** the tabPanes, so that it will be possible to have multiple tabPanes */
  // TODO: implement
  private final ObservableList<TabPane> tabPanes
          = FXCollections.observableArrayList();

  /** a field to remember the current tabPane */
  private TabPane currentTabPane;

  /** a Property to remember the current tab */
  private ObjectProperty<RudiTab> currentTab
          = new SimpleObjectProperty<>();
  public ObjectProperty<RudiTab> currentTabProperty() { return currentTab; }


  public TabStore(HBox ap) {
    tabBox = ap;
//    openTabs = new HashMap<>();
  }

  /** This makes sure to add the new tab to the right tabPane */
  private void addToTabPane(RudiTab tab) {
    if (currentTabPane == null) {
      TabPane tp = new TabPane();
      tp.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
      HBox.setHgrow(tp, Priority.ALWAYS);
      tabBox.getChildren().add(tp);
      currentTabPane = tp;

      /* define a listener to automatically transmit the selected RudiTab */
      currentTabPane.getSelectionModel().selectedItemProperty()
              .addListener((o, oldVal, newVal) -> {
        currentTabProperty().setValue((RudiTab) newVal);
      });
    }
    currentTabPane.getTabs().add(tab);

  }

  public void openTab(FileAtPos request) {
    if (openTabs.getValue().keySet().contains(request.getFile())) {
      RudiTab requestedTab = openTabs.getValue().get(request.getFile());

      requestedTab._codeArea.showParagraphPretty(request.getPosition()-1);
      requestedTab._codeArea.moveTo(request.getPosition()-1, 0);

      if (! requestedTab.getTabPane().getSelectionModel().getSelectedItem()
              .equals(requestedTab)) {
        requestedTab.getTabPane().getSelectionModel().select(requestedTab);
        log.debug("Switched to requested tab.");
      }

    } else {
      RudiTab newTab = new RudiTab(request.getFile());
      newTab.setContent();
      newTab.setOnCloseRequest((Event arg0) -> {
        closeTab(newTab);
      });

      addToTabPane(newTab);

      newTab._codeArea.showParagraphPretty(request.getPosition()-1);
      newTab._codeArea.moveTo(request.getPosition()-1, 0);
      newTab.getTabPane().getSelectionModel().select(newTab);

      openTabs.getValue().put(request.getFile(), newTab);
      log.debug("Created requested tab.");
    }
  }

  public void closeTab(RudiTab tab) {
    openTabs.getValue().remove(tab.getFile());
  }
}

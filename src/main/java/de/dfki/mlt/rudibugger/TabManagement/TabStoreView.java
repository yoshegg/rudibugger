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

import static de.dfki.mlt.rudibugger.TabManagement.TabManager.log;
import java.nio.file.Path;
import java.util.HashMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * This class defines the look of rudibugger's tabs. It is connected to
 * <code>TabManager</code> via listeners to seperate view and model.
 * <code>TabStoreView</code> opens and closes tabs and is also responsible of
 * the organization of these tabs.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class TabStoreView {

  /** The underlying ground of the <code>TabPane</code>s. */
  private final HBox _tabBox;

  /** Contains all <code>TabPane</code>s. */
  private final ObservableList<TabPane> _tabPanes
          = FXCollections.observableArrayList();

  /** The currently active <code>TabPane</code>. */
  private TabPane _currentTabPane;


  /*****************************************************************************
   * INITIALIZER
   ****************************************************************************/

  /** Creates a new TabStoreView object. */
  public TabStoreView(HBox hb) {
    _tabBox = hb;
  }

  /** Initializes by linking properties and defining listeners. */
  public void initialize(TabManager ts) {
    linkToTabStore(ts);
    defineListeners();
  }

  /** Links properties from TabStore to TabStoreView's properties. */
  private void linkToTabStore(TabManager ts) {
    ts.currentlySelectedTabProperty().bindBidirectional(currentlySelectedTab);
    ts.openTabsProperty().bindBidirectional(openTabs);
    ts.requestedFileProperty().bindBidirectional(requestedFile);
    ts.requestedClosingOfTabProperty().bindBidirectional(requestedClosingOfTab);
  }

  /** Defines what should be done when requesting a tab related action. */
  private void defineListeners() {
    requestedFile.addListener((cl, ov, nv) -> { if (nv != null) openTab(nv); });
    requestedClosingOfTab.addListener((cl, ov, nv) -> closeTab(nv) );
  }

  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Opens a new tab or switches to an already existing tab. */
  public void openTab(FileAtPos request) {
    if (openTabs.getValue().keySet().contains(request.getFile()))
      switchToTab(request);
    else
      createTab(request);
  }

  /** Closes a given tab. */
  public void closeTab(RudiTab tab) {
    openTabs.getValue().remove(tab.getFile());
    log.debug("Closed tab.");
  }

  /** Creates a new tab. */
  private void createTab(FileAtPos request) {
    RudiTab newTab = new RudiTab(request.getFile());
    newTab.setContent();
    newTab.setOnCloseRequest(e -> closeTab(newTab));

    addToTabPane(newTab);

    newTab.getCodeArea().showParagraphPretty(request.getPosition() - 1);
    newTab.getCodeArea().moveTo(request.getPosition() - 1, 0);
    newTab.getTabPane().getSelectionModel().select(newTab);

    openTabs.getValue().put(request.getFile(), newTab);
    log.debug("Created requested tab.");
  }

  /** Switches to an already open tab. */
  private void switchToTab(FileAtPos request) {
    RudiTab requestedTab = openTabs.get().get(request.getFile());

    requestedTab.getCodeArea().showParagraphPretty(request.getPosition() - 1);
    requestedTab.getCodeArea().moveTo(request.getPosition() - 1, 0);

    if (!requestedTab.getTabPane().getSelectionModel().getSelectedItem()
            .equals(requestedTab)) {
      requestedTab.getTabPane().getSelectionModel().select(requestedTab);
      log.debug("Switched to requested tab.");
    }
  }

  /** Adds the new tab to the current tabPane */
  private void addToTabPane(RudiTab tab) {
    if (_currentTabPane == null)
      createNewTabPane();
    _currentTabPane.getTabs().add(tab);
  }

  /** Creates a new <code>TabPane</code>. */
  private void createNewTabPane() {
    TabPane tp = new TabPane();
      tp.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
      HBox.setHgrow(tp, Priority.ALWAYS);
      _tabBox.getChildren().add(tp);
      _currentTabPane = tp;

      /* define a listener to automatically transmit the selected RudiTab */
      _currentTabPane.getSelectionModel().selectedItemProperty()
              .addListener((o, oldVal, newVal) -> {
        currentlySelectedTab.set((RudiTab) newVal);
      });
  }


  /*****************************************************************************
   * PROPERTIES
   ****************************************************************************/

  /** Represents the currently selected tab. */
  private final ObjectProperty<RudiTab> currentlySelectedTab =
          new SimpleObjectProperty<>();

  /** Maps a Path to a currently open tab. */
  private final ObjectProperty<HashMap<Path, RudiTab>> openTabs
          = new SimpleObjectProperty<>(new HashMap<>());

  /** Represents a tab that is requested to be closed. */
  private final ObjectProperty<RudiTab> requestedClosingOfTab =
          new SimpleObjectProperty<>();

  /** Represents a file to be opened as tab or to be switched to. */
  private final ObjectProperty<FileAtPos> requestedFile
          = new SimpleObjectProperty<>();


}

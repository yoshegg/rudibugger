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

import de.dfki.mlt.rudibugger.DataModel;
import java.nio.file.Path;
import java.util.HashMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for managing tabs:
 * - only 1 tab per file
 * - manage multiple TabPanes
 * - switch to requested tab
 *
 * TODO: Does not need the Model
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class TabManager {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("TabStore");

  /** The <code>DataModel</code>. */
  private final DataModel _model;


  /*****************************************************************************
   * INITIALIZER
   ****************************************************************************/

  /**
   * Initializes this project specific addition of <code>DataModel</code>.
   *
   * @param model The current <code>DataModel</code>
   */
  public TabManager(DataModel model) { _model = model; }


  /*****************************************************************************
   * PROPERTIES
   ****************************************************************************/

  /** Represents the currently selected tab. */
  private final ObjectProperty<RudiTab> currentlySelectedTab =
          new SimpleObjectProperty<>();

  /** Maps a Path to a currently open tab. */
  private final ObjectProperty<HashMap<Path, RudiTab>> openTabs
          = new SimpleObjectProperty<>();

  /** Represents a tab that is requested to be closed. */
  private final ObjectProperty<RudiTab> requestedClosingOfTab =
          new SimpleObjectProperty<>();

  /** Represents a file to be opened as tab or to be switched to. */
  private final ObjectProperty<FileAtPos> requestedFile
          = new SimpleObjectProperty<>();

  /** Represents a tab that is requested to be saved. */
  private final ObjectProperty<RudiTab> requestedSavingOfTab =
          new SimpleObjectProperty<>();


  /*****************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS AND PROPERTIES
   ****************************************************************************/

  /** @Return The currently selected tab. */
  public ObjectProperty<RudiTab> currentlySelectedTabProperty() {
    return currentlySelectedTab;
  }

  /** @Return A Map containing all open files and their respective tabs. */
  public ObjectProperty<HashMap<Path, RudiTab>> openTabsProperty() {
    return openTabs;
  }

  /** @Return A tab that should be closed. */
  public ObjectProperty<RudiTab> requestedClosingOfTabProperty() {
    return requestedClosingOfTab;
  }

  /** @Return A file that should be opened at a certain position. */
  public ObjectProperty<FileAtPos> requestedFileProperty() {
    return requestedFile;
  }

  /** @Return A tab that should be closed. */
  public ObjectProperty<RudiTab> requestedSavingOfTabProperty() {
    return requestedSavingOfTab;
  }

}

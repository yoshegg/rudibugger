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
package de.dfki.mlt.rudibugger;

import static de.dfki.mlt.rudibugger.Constants.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.scene.control.SplitPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality concerning the storage of layout settings.
 * TODO: Only needs yaml and stage
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ViewLayout {

  /** The Logger. */
  static Logger log = LoggerFactory.getLogger("ViewLayout");

  /** The <code>DataModel</code>. */
  private final DataModel _model;


  /*****************************************************************************
   * FIELDS
   ****************************************************************************/

  /** Contains all layout settings. */
  private HashMap<String, Double> layoutConfiguration = new HashMap();

  /** Represents the SplitPane separating editor and sidebar. */
  private SplitPane _sidebarEditorSplitPane;

  /** Represents the SplitPane separating ruleTreeView and fileTreeView. */
  private SplitPane _sidebarSplitPane;

  /*****************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   ****************************************************************************/

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public ViewLayout(DataModel model) {
    _model = model;
  }


  /*****************************************************************************
   * CONSTANTS
   ****************************************************************************/

  /** Position of rudibugger window on the x-axis. */
  private static final String WINDOW_POSITION_X = "Window_Position_X";

  /** Position of rudibugger windows on the y-axis. */
  private static final String WINDOW_POSITION_Y = "Window_Position_Y";

  /** Width of the rudibugger window. */
  private static final String WINDOW_WIDTH = "Window_Width";

  /** Height of the rudibugger window. */
  private static final String WINDOW_HEIGHT = "Window_Height";

  /** SplitPane dividing sidebar and editor */
  public static final String DIVIDER_SIDEBAR_EDITOR = "Divider_Sidebar_Editor";

  /** SplitPane dividing sidebar and editor */
  public static final String DIVIDER_SIDEBAR = "Divider_Sidebar";


  /*****************************************************************************
   * METHODS
   *****************************************************************************/

  /** Restores the window's position if a configuration file exists. */
  public void restoreWindowPosition() {
    if (GLOBAL_LAYOUT_CONFIG_FILE.toFile().length() != 0) {
      try {
        layoutConfiguration = (HashMap<String, Double>) _model.yaml.load(
                new FileReader(GLOBAL_LAYOUT_CONFIG_FILE.toFile()));
      } catch (FileNotFoundException
             | org.yaml.snakeyaml.error.YAMLException e) {
        log.error(e.getMessage());
        return;
      }

      _model.mainStage.setX(layoutConfiguration.get(WINDOW_POSITION_X));
      _model.mainStage.setY(layoutConfiguration.get(WINDOW_POSITION_Y));
      _model.mainStage.setWidth(layoutConfiguration.get(WINDOW_WIDTH));
      _model.mainStage.setHeight(layoutConfiguration.get(WINDOW_HEIGHT));
    }
  }

  /** Restores the dividers' positions if a configuration file exists. */
  public void restoreDividerPositions() {
    if (GLOBAL_LAYOUT_CONFIG_FILE.toFile().length() != 0) {
      try {
        layoutConfiguration = (HashMap<String, Double>) _model.yaml.load(
                new FileReader(GLOBAL_LAYOUT_CONFIG_FILE.toFile()));
      } catch (FileNotFoundException
             | org.yaml.snakeyaml.error.YAMLException e) {
        log.error(e.getMessage());
        return;
      }

      Platform.runLater(() -> {
        _sidebarSplitPane.setDividerPositions(
              layoutConfiguration.get(DIVIDER_SIDEBAR));
          // TODO: does not work, JavaFX bug
        _sidebarEditorSplitPane.setDividerPositions(
              layoutConfiguration.get(DIVIDER_SIDEBAR_EDITOR));
      });
    } else {
      initializeDividersPosition();
    }
  }

  /** Initializes the layout. */
  private void initializeDividersPosition() {
    _sidebarEditorSplitPane.setDividerPositions(0.30);
  }

  /**
   * Defines a listener that automatically saves the layout configuration if the
   * close button has been pressed.
   */
  public void setStageCloseListener() {
    _model.mainStage.setOnCloseRequest(e -> saveLayoutToFile());
  }

  /** Saves the window's layout. */
  public void saveLayoutToFile() {
    layoutConfiguration.put(WINDOW_POSITION_X, _model.mainStage.getX());
    layoutConfiguration.put(WINDOW_POSITION_Y, _model.mainStage.getY());
    layoutConfiguration.put(WINDOW_WIDTH, _model.mainStage.getWidth());
    layoutConfiguration.put(WINDOW_HEIGHT, _model.mainStage.getHeight());
    layoutConfiguration.put(DIVIDER_SIDEBAR_EDITOR,
            _sidebarEditorSplitPane.getDividerPositions()[0]);
    layoutConfiguration.put(DIVIDER_SIDEBAR,
            _sidebarSplitPane.getDividerPositions()[0]);
    try {
      FileWriter writer = new FileWriter(GLOBAL_LAYOUT_CONFIG_FILE.toFile());
      _model.yaml.dump(layoutConfiguration, writer);
    } catch (IOException ex) {
      log.error("Could not save layout.");
    }
    log.debug("Saved layout settings.");
  }

  /** Links a <code>SplitPane</code> to model.layout.
   * @param sp The <code>SplitPane</code> to link
   * @param s The description of the <code>SplitPane</code>
   */
  public void addSplitPane(SplitPane sp, String s) {
    switch (s) {
      case DIVIDER_SIDEBAR_EDITOR:
        _sidebarEditorSplitPane = sp;
        break;
      case DIVIDER_SIDEBAR:
        _sidebarSplitPane = sp;
        break;
      default:
        log.debug("Unknown splitPane.");
    }
  }

}

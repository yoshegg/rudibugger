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

package de.dfki.mlt.rudibugger.DataModelAdditions;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.DataModel;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality concerning global configuration.
 * Global configuration means configuration of rudibugger that is used by
 * rudibugger regardless of the currently loaded project.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class GlobalConfiguration {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("GlobalConf");

  /** The <code>DataModel</code> */
  private final DataModel _model;

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public GlobalConfiguration(DataModel model) {
    _model = model;
    loadGlobalConfiguration();
    loadRecentProjects();
    keepUpdated();
  }

  /**
   * Defines listeners to automatically update relative files and properties on
   * changes.
   */
  private void keepUpdated() {
    _globalConfigs.addListener((MapChangeListener) (cl -> {
      saveGlobalConfiguration();
    }));
    recentProjects.addListener((ListChangeListener) (cl -> {
      saveRecentProjects();
    }));

  }


  /*****************************************************************************
   * GLOBAL CONFIGURATION
   ****************************************************************************/

  /** Contains all global configuration data. */
  private ObservableMap<String, Object> _globalConfigs;

  /**
   * Loads a global configuration file (if it exists) and checks it for
   * completeness or creates a new one.
   */
  private void loadGlobalConfiguration() {
    try {
      Map tempMap = (Map<String, Object>) _model.yaml.load(
        new FileInputStream(GLOBAL_CONFIG_FILE.toFile()));
      _globalConfigs = FXCollections.observableMap(tempMap);
      checkConfigForCompleteness();
    } catch (FileNotFoundException ex) {
      log.error("No configuration file has been found. Creating a new one...");
      _globalConfigs
        = FXCollections.observableMap(DEFAULT_GLOBAL_CONFIGURATION);
      saveGlobalConfiguration();
    }

    timeStampIndex.bindBidirectional(
      (BooleanProperty) _globalConfigs.get("timeStampIndex")
    );
  }

  /** Saves the global configuration in a file. */
  private void saveGlobalConfiguration() {
     try {
      FileWriter writer = new FileWriter(GLOBAL_CONFIG_FILE.toFile());
      _model.yaml.dump(_globalConfigs, writer);
    } catch (IOException ex) {
       log.error("Could not save global configuration file.");
    }
  }

  /**
   * Checks if the current configuration map is up to date and updates it if
   * needed. If it was updated, it will also be safed.
   */
  private void checkConfigForCompleteness() {
    boolean save = false;
    for (String s : DEFAULT_GLOBAL_CONFIGURATION.keySet()) {
      if (! _globalConfigs.containsKey(s)) {
        save = true;
        _globalConfigs.put(s, DEFAULT_GLOBAL_CONFIGURATION.get(s));
      }
    }
    if (save) saveGlobalConfiguration();
  }

  /**
   * Updates a global setting.
   *
   * @param key    The setting
   * @param value  The new value
   */
  public void setSetting(String key, Object value) {
    if (! _globalConfigs.containsKey(key)) {
      log.error("Refuse to set unkown setting: " + key);
      return;
    }
    // TODO: Not so nice
    if (key.equals("timeStampIndex")) {
      ((BooleanProperty) _globalConfigs.get(key)).set((boolean) value);
      saveGlobalConfiguration();
    } else {
      _globalConfigs.put(key, value);
    }
  }

  /** Contains the default values for the global configuration file. */
  private static final HashMap<String, Object> DEFAULT_GLOBAL_CONFIGURATION =
          new HashMap<String, Object>() {{
      put("editor", "rudibugger");
      put("openFileWith", "");
      put("openRuleWith", "");
      put("timeStampIndex", new SimpleBooleanProperty(true));
      put("saveOnCompile", 2);
      put("lastOpenedProject", null);
    }};


  /*****************************************************************************
   * RECENT PROJECTS
   ****************************************************************************/

  /** Represents the last opened projects. */
  public ObservableList<String> recentProjects;

  /** Loads the recent projects. */
  private void loadRecentProjects() {
    try {
      ArrayList tempList = (ArrayList) _model.yaml.load(
            new FileInputStream(RECENT_PROJECTS_FILE.toFile()));
      recentProjects = FXCollections.observableArrayList(tempList);
    } catch (FileNotFoundException ex) {
      log.error("No recent projects file has been found. Creating a new "
              + "one...");
      recentProjects = FXCollections.observableArrayList();
      saveRecentProjects();
    }
  }

  /** Saves the recent projects list in a file. */
  private void saveRecentProjects() {
    try {
      FileWriter writer = new FileWriter(RECENT_PROJECTS_FILE.toFile());
      _model.yaml.dump(recentProjects, writer);
    } catch (IOException ex) {
       log.error("Could not save recent projects file.");
    }
  }

  /**
   * Add project to list of recent projects.
   *
   * @param project  The project to add
   */
  public void addToRecentProjects(Path project) {
    String projPath = project.toString();
    if (recentProjects.contains(projPath)) {
      recentProjects.remove(projPath);
    }
    recentProjects.add(0, projPath);
  }


  /*****************************************************************************
   * FIELDS REPRESENTING SETTINGS
   ****************************************************************************/

  /** @return The specified editor in the global configuration. */
  public String getEditor() {return (String) _globalConfigs.get("editor");}

  /**
   * Defines whether or not an index should be shown in the log in case of
   * simultaneous logs.
   */
  private final BooleanProperty timeStampIndex = new SimpleBooleanProperty();

  /**
   * @return The property indicating whether or not an index should be shown in
   * the log in case of simultaneous logs
   */
  public BooleanProperty timeStampIndexProperty() {
    return timeStampIndex;
  }

  /** @return A custom editor (not rudibugger or emacs). */
  public String getOpenFileWith() {
    return (String) _globalConfigs.get("openFileWith");
  }

  /** @return A custom editor (not rudibugger or emacs). */
  public String getOpenRuleWith() {
    return (String) _globalConfigs.get("openRuleWith");
  }

  /** @return The project that will be reopened when reopening rudibugger. */
  public Path getLastOpenedProject() {
    String s = (String) _globalConfigs.get("lastOpenedProject");
    if (s != null && !"".equals(s))
      return Paths.get(s);
    else
      return null;
  }

  /** @return How unsaved files should be treated before compiling. */
  public int getSaveOnCompile() {
    return (int) _globalConfigs.get("lastOpenedProject");
  }

}

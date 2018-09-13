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

import de.dfki.mlt.rudibugger.editor.EmacsConnection;
import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.editor.CustomEditor;
import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudibugger.editor.Editor;
import de.dfki.mlt.rudibugger.editor.EmacsEditor;
import de.dfki.mlt.rudibugger.editor.RudibuggerEditor;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.property.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class DataModel {


  /* ***************************************************************************
   * BASIC FIELDS
   * **************************************************************************/

  static Logger log = LoggerFactory.getLogger("DataModel");

  /** The main stage, necessary when opening additional windows e.g. prompts. */
  public Stage mainStage;


  /* ***************************************************************************
   * PROPERTIES
   * **************************************************************************/

  /** Represents a loaded project. */
  private final ObjectProperty<Project> _loadedProject
          = new SimpleObjectProperty<>();

  /** Represents the text shown on the status bar. */
  private final StringProperty statusBarMessage
          = new SimpleStringProperty();

  /** Used to open, edit and save files. */
  private Editor _editor;


  /* ***************************************************************************
   * CONFIGURATION DETAILS
   * **************************************************************************/

  /** Stores information about rudibugger's layout. TODO not nice */
  public final ViewLayout layout;

//  /** Provides additional functionality to interact with Emacs. */
//  public EmacsConnection emacs = new EmacsConnection(this);

  /** Provides additional functionality concerning global configuration. */
  public GlobalConfiguration globalConf = new GlobalConfiguration(this);


  /* ***************************************************************************
   * CONSTRUCTOR
   * **************************************************************************/

  /** Creates a new <code>DataModel</code>. */
  public DataModel(Stage stage) {
    mainStage = stage;
    layout = new ViewLayout(mainStage); // TODO: not nice?
    if (! Files.exists(GLOBAL_CONFIG_PATH)) createGlobalConfigFolder();
    setEditor();
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  public void openProject(Path projectYamlPath) {
    if (_loadedProject.get() != null) {
      if (OVERWRITE_PROJECT != HelperWindows.openOverwriteProjectCheckDialog(
              _loadedProject.get().getProjectName()))
        return;
    }
    Project newProject = Project.openProject(projectYamlPath);
    if (newProject == null) return; // Project could not be opened
    _loadedProject.set(newProject);
    if (newProject.getRuleModel() != null
        && globalConf.getAutomaticallyConnectToVonda())
      getLoadedProject().vonda.connect(getLoadedProject().getVondaPort());
  }

  public void closeProject() {
    if (_loadedProject.get() != null) {
      _loadedProject.get().closeProject();
      HelperWindows.closeRuleLoggingWindow();
      _loadedProject.set(null);
    }
  }

  /** Starts a wizard to create a new VOnDA compatible project from scratch. */
  public void createNewProject() {
    log.info("Not implemented yet.");
    // TODO
  }

  private void setEditor() {
    switch (globalConf.getEditor()) {
      case "rudibugger":
        _editor = RudibuggerEditor.getNewEditor();
        return;
      case "emacs":
        _editor = EmacsEditor.getNewEditor(this);
        return;
      case "custom":
        _editor = CustomEditor.getNewEditor(globalConf);
    }
  }

  private void createGlobalConfigFolder() {
    GLOBAL_CONFIG_PATH.toFile().mkdirs();
    log.info("Created global config folder (first start of rudibugger");
  }


  /* ***************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS AND PROPERTIES
   * **************************************************************************/

  /** @return Represents the text shown on the statusBar. */
  public StringProperty statusBarTextProperty() { return statusBarMessage; }

  /** @return The currently loaded project (or null) */
  public Project getLoadedProject() { return _loadedProject.get(); }

  /** Contains the currently loaded project. */
  public ObjectProperty<Project> loadedProjectProperty() {
    return _loadedProject;
  }

  /** @return True, if a project has been loaded, else false. */
  public boolean isProjectLoaded() { return _loadedProject.get() != null; }

  /** @return The current editor instance. */
  public Editor getEditor() { return _editor; }

}

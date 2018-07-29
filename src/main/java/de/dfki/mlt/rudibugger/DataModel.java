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

import de.dfki.mlt.rudibugger.Project.Project;
import de.dfki.mlt.rudibugger.DataModelAdditions.*;
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
   * CONFIGURATION DETAILS
   * **************************************************************************/

  /** Stores information about rudibugger's layout. */
  public final ViewLayout layout;

  /** Provides additional functionality to interact with Emacs. */
  public EmacsConnection emacs = new EmacsConnection(this);

  /** Provides additional functionality concerning global configuration. */
  public GlobalConfiguration globalConf = new GlobalConfiguration(this);


  /* ***************************************************************************
   * PROPERTIES
   * **************************************************************************/

  /** Represents the text shown on the status bar. */
  private final StringProperty statusBarMessage
          = new SimpleStringProperty();


  /* ***************************************************************************
   * CONSTRUCTOR
   * **************************************************************************/

  /** Creates a new <code>DataModel</code>. */
  public DataModel(Stage stage) {
    mainStage = stage;
    layout = new ViewLayout(mainStage);
//    _project.rudiSave.initSaveListener(); // TODO

  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  public void openProject(Path projectYamlPath) {
    Project.openProject(projectYamlPath, globalConf);
    if (globalConf.getAutomaticallyConnectToVonda())
      getCurrentProject().vonda.connect(getCurrentProject().getVondaPort());
    getCurrentProject().linkEmacs(emacs); // TODO not nice
  }

  public void closeProject() {
    Project.closeProject();
  }

  /** Starts a wizard to create a new VOnDA compatible project from scratch. */
  public void createNewProject() {
    log.info("Not implemented yet.");
    // TODO
  }


  /* ***************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS AND PROPERTIES
   * **************************************************************************/

  /** @return Represents the text shown on the statusBar. */
  public StringProperty statusBarTextProperty() { return statusBarMessage; }

  public Project getCurrentProject() { return Project.getCurrentProject(); }

  public BooleanProperty isProjectLoadedProperty() {
    return Project.projectLoadedProperty();
  }

}

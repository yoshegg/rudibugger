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

package de.dfki.mlt.rudibugger.project;

import static de.dfki.mlt.rudibugger.Constants.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality to start VOnDAs compiler.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class VondaCompiler {

  static Logger log = LoggerFactory.getLogger("vondaCompile");


  /* ***************************************************************************
   * FIELDS AND PROPERTIES
   * **************************************************************************/

  /** The associated project. */
  private final Project _project;

  /** Represents the compilation process. */
  private Process _p;

  /**
   * Contains all compile commands: a potential compile file and additional
   * commands in the project's configuration file.
   */
  private final Map<String, String> _compileCommandsMap = new HashMap<>();

  /**
   * Reflects the order of the compile commands of the compile button. The
   * first entry represents the default command.
   */
  private final ObservableList<String> _compileCommandsList
    = FXCollections.observableArrayList();


  /* ***************************************************************************
   * CONSTRUCTOR
   * **************************************************************************/

  /** Initializes an instance of this class. */
  public VondaCompiler(Project project) {
    _project = project;
    retrieveCompileCommands();
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  private void retrieveCompileCommands() {
    Path compileScript = _project.getRootFolder().resolve(COMPILE_FILE);
    if (Files.exists(compileScript))
      _compileCommandsMap.put("Compile", compileScript.toAbsolutePath()
              .toString());
    _compileCommandsMap.putAll(_project.getCustomCompileCommands());
    _compileCommandsList.addAll(_compileCommandsMap.keySet());
    if (_project.getDefaultCompileCommand() != null)
      setDefaultCompileCommand(_project.getDefaultCompileCommand());
    _compileCommandsList.addListener((ListChangeListener) cl ->
      _project.setDefaultCompileCommand(_compileCommandsList.get(0))
    );
  }

  /**
   * Starts VOnDAs compilation process by using a given command.
   *
   * @param inputCmd  A custom command
   * @throws IOException
   * @throws InterruptedException
   */
  public void startCompile(String inputCmd) {
//    _project.quickSaveAllFiles(); TODO

    if ((_p != null) && (_p.isAlive())) {
      log.debug("Aborting current compilation process...");
      _p.destroy();
    }

    String command = "bash -c '"
          + "cd " + _project.getRootFolder().toString() + ";"
          + inputCmd + ";"
          + "read -n1 -r -p \"Press any key to continue...\" key;'";

    log.info("Starting compilation...");
    File mateTerminal = new File("/usr/bin/mate-terminal");
    String windowTitle = "Compiling " + _project.getProjectName();
    String titleOpt = "-T";

    try {
      if ("Linux".equals(System.getProperty("os.name"))) {
        String[] cmd;
        String termString = "/usr/bin/xterm";
        if (mateTerminal.exists()) {
          termString = "/usr/bin/mate-terminal";
          titleOpt = "-t";
        }
        cmd = new String[] { termString, titleOpt, windowTitle, "-e", command};

        log.debug("Executing the following command: " + Arrays.toString(cmd));

        _p = Runtime.getRuntime().exec(cmd);
      } else {
        _p = Runtime.getRuntime().exec(inputCmd);
      }
    } catch (IOException ex) {
        log.error(ex.getMessage());
    }
  }

  /* ***************************************************************************
   * GETTERS / SETTERS
   * **************************************************************************/

  public void setDefaultCompileCommand(String label) {
    if (_compileCommandsList.contains(label))
      _compileCommandsList.remove(label);
    _compileCommandsList.add(0, label);
  }

  public String getDefaultCompileCommandLabel() {
    return _compileCommandsList.get(0);
  }

  public String getDefaultCompileCommand() {
    return getCompileCommand(getDefaultCompileCommandLabel());
  }

  public Collection<String> getCompileCommandLabels() {
    return _compileCommandsMap.keySet();
  }

  /** @return the real command to execute for a compile command label */
  public String getCompileCommand(String label) {
    return _compileCommandsMap.get(label);
  }

  public ObservableList<String> getCompileLabelList() {
    return _compileCommandsList;
  }

}

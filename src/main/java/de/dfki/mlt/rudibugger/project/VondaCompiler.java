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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality to start VOnDAs compiler.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class VondaCompiler {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("vondaCompile");

  /** The associated project. */
  private final Project _project;

  /** Represents the compilation process. */
  private Process _p;

  /**
   * TODO
   */
  public VondaCompiler(Project project) { _project = project; }

  /**
   * Starts the default compilation process as specified in the project's YAML
   * configuration file.
   */
//  public void startCompileFileBasedCompilation() {
//    String compileScript = _model.project.getCompileFile().toString();
//    startCompile(compileScript);
//  }

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
                //_model.project.getCompileFile().toString());
      }
    } catch (IOException ex) {
        log.error(ex.getMessage());
    }
  }

}

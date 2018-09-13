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
package de.dfki.mlt.rudibugger.editor;

import de.dfki.mlt.rudibugger.GlobalConfiguration;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the functionality specified in the settings dialog of rudibugger.
 * At the moment, only loading functions are supported.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class CustomEditor extends Editor {

  static Logger log = LoggerFactory.getLogger("CustomEditor");

  GlobalConfiguration _globalConf;

  /* ***************************************************************************
   * CONSTRUCTOR & OTHER METHODS
   * **************************************************************************/

  public static CustomEditor getNewEditor(GlobalConfiguration globalConf) {
    return new CustomEditor(globalConf);
  }

  private CustomEditor(GlobalConfiguration globalConf) {
    _globalConf = globalConf;
  }


  /* ***************************************************************************
   * SAVE METHODS
   * **************************************************************************/

  @Override
  public void saveFile() {
    log.error("Unsupported operation, not implemented (yet).");
  }

  @Override
  public void saveFileAs(Path file) {
    log.error("Unsupported operation, not implemented (yet).");
  }

  @Override
  public void saveAllFiles() {
    log.error("Unsupported operation, not implemented (yet).");
  }


  /* ***************************************************************************
   * LOAD METHODS
   * **************************************************************************/

  @Override
  public void createNewFile() {
    log.error("Unsupported operation, not implemented (yet).");
  }

  @Override
  public void loadFile(Path file) {
    try {
      String cmd = (_globalConf.getOpenFileWith())
        .replaceAll("%file", file.toString());
      Runtime.getRuntime().exec(cmd);
    } catch (IOException ex) {
      log.error("Can't use custom editor to open file. ");
    }
  }

  @Override
  public void loadFileAtLine(Path file, int line) {
    try {
      String cmd = (_globalConf.getOpenRuleWith())
        .replaceAll("%file", file.toString())
        .replaceAll("%line", Integer.toString(line));
      Runtime.getRuntime().exec(cmd);
    } catch (IllegalArgumentException ex) {
      log.error("Can't use custom editor to open file at line. "
        + "Trying to open file without specified line.");
      loadFile(file);
    } catch (IOException ex) {
      log.error("Can't use custom editor to open file. ");
    }
  }


  /* ***************************************************************************
   * CLOSE METHODS
   * **************************************************************************/

  @Override
  public void closeFile() {
    log.error("Unsupported operation, not implemented (yet).");
  }

  @Override
  public void closeAllFiles() {
    log.error("Unsupported operation, not implemented (yet).");
  }

}

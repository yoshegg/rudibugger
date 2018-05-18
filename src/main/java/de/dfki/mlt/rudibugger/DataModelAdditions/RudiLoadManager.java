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

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.TabManagement.FileAtPos;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality to load .rudi files and rules.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiLoadManager {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("rudiLoad");

  /** The <code>DataModel</code> */
  private final DataModel _model;

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public RudiLoadManager(DataModel model) { _model = model; }

  /**
   * Opens a new tab (or an already opened tab) showing a given file in
   * rudibugger.
   *
   * @param file The wanted file
   */
  private void requestTabOfFile(Path file) {
    requestTabOfRule(file, 1);
  }

  /**
   * Opens a new tab (or an already opened tab) showing a certain rule of a
   * given file in rudibugger.
   *
   * @param file      The wanted file
   * @param position  The line of the wanted rule
   */
  private void requestTabOfRule(Path file, Integer position) {
    FileAtPos temp = new FileAtPos(file, position);
    _model.tabStore.requestedFileProperty().set(temp);
  }

  /**
   * Opens a given file in an editor defined in the settings of rudibugger.
   *
   * @param file the wanted file
   */
  public void openFile(Path file) {
    switch (_model.globalConf.getEditor()) {
      case "rudibugger":
        requestTabOfFile(file);
        return;
      case "emacs":
        if (! _model.emacs.isAlive()) {
          _model.emacs.startConnection("emacs");
        }
        _model.emacs.getConnector().visitFilePosition(file.toFile(), 1, 0, "");
        return;
      case "custom":
        try {
          String cmd = (_model.globalConf.getOpenFileWith())
                  .replaceAll("%file", file.toString());
          Runtime.getRuntime().exec(cmd);
          return;
        } catch (IOException ex) {
          log.error("Can't use custom editor to open file. ");
          break;
        }
      default:
        break;
    }
    log.info("No valid file editor setting has been found. Using rudibugger.");
        requestTabOfFile(file);
  }

  /**
   * Opens a given file at a specific line in an editor defined in the settings
   * of rudibugger.
   *
   * @param file the wanted file
   * @param position the line of the wanted rule
   */
  public void openRule(Path file, Integer position) {
    switch (_model.globalConf.getEditor()) {
      case "rudibugger":
        requestTabOfRule(file, position);
        return;
      case "emacs":
        if (! _model.emacs.isAlive()) {
          _model.emacs.startConnection("emacs");
        }
        _model.emacs.getConnector().visitFilePosition(file.toFile(), position, 0, "");
        return;
      case "custom":
        try {
          String cmd = (_model.globalConf.getOpenRuleWith())
                  .replaceAll("%file", file.toString())
                  .replaceAll("%line", position.toString());
          Runtime.getRuntime().exec(cmd);
          return;
        } catch (IOException ex) {
          log.error("Can't use custom editor to open file. ");
          break;
        }
      default:
        break;
    }
    log.info("No valid file editor setting has been found. Using rudibugger.");
    requestTabOfRule(file, position);
  }
}

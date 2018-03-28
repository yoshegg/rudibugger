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

import de.dfki.lt.j2emacs.J2Emacs;
import java.io.File;

/**
 * Provides additional functionality to interact with Emacs.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class EmacsConnection {

  /** An Emacs connector. */
  private J2Emacs _j2e = null;

  /**
   * @return The Emacs connector (instance of class <code>J2Emacs</code>)
   */
  public J2Emacs getConnector() { return _j2e; }

  /**
   * Starts connection to Emacs.
   *
   * @param emacsPath
   */
  public void startConnection(String emacsPath) {
    File emacsLispPath = new File("src/main/resources/emacs/");
    _j2e = new J2Emacs("Rudibugger", emacsLispPath, null);
    _j2e.addStartHook(
        "(setq auto-mode-alist (append (list '(\"\\\\.rudi\" . java-mode))))");
    _j2e.startEmacs();
  }

  /**
   * Checks if Emacs is running.
   *
   * @return True, if emacs running, else false
   */
  public boolean isAlive() {
    return _j2e != null && _j2e.alive();
  }

  /**
   * Closes Emacs.
   *
   * @param quitEmacs
   */
  public void close(boolean quitEmacs) {
    if (_j2e == null) return;
    if (quitEmacs) {
      _j2e.exitEmacs();
    }
    _j2e.close();
    _j2e = null;
  }
}

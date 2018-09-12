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
package de.dfki.mlt.rudibugger.tracking;

import de.dfki.mlt.rudibugger.GlobalConfiguration;
import de.dfki.mlt.rudibugger.editor.Editor;
import de.dfki.mlt.rudibugger.project.Project;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class TrackingController {

  static Logger log = LoggerFactory.getLogger("Tracking");

  /** Is used to open a selected entry (if possible). */
  private Editor _editor;


  /* ***************************************************************************
   * GUI ELEMENTS
   * **************************************************************************/

  /** Used for complex / custom queries. */
  private TextField queryTextField;

  /** Shows the tracked elements. */
  private TreeTableView trackingTreeTableView;

  /* Columns. */
  private TreeTableColumn queryColumn;
  private TreeTableColumn valueColumn;
  private TreeTableColumn dateColumn;

  /** Contains predefined elements. */
  private MenuButton debugMenuButton;


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  public void init(Editor editor) {
    _editor = editor;

  }

}

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

package de.dfki.mlt.rudibugger.Controller;

import de.dfki.mlt.rudibugger.DataModel;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the about window.
 *
 * TODO:
 *   - Add mailto
 *   - Rework design
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class AboutController {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("About");

  /** The <code>DataModel</code>. */
  private DataModel _model = null;

  /** The stage. */
  private Stage _stage;

  /**
   * Initializes the controller.
   */
  public void initModel(DataModel model) {
    if (_model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    _model = model;
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Binds the stage to the controller. */
  public void setDialogStage(Stage dialogStage) {
    _stage = dialogStage;
  }

}

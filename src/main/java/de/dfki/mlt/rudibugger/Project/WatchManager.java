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
package de.dfki.mlt.rudibugger.Project;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.Project.WatchServices.RudiFolderWatch;
import static de.dfki.mlt.rudibugger.Project.WatchServices.RudiFolderWatch.createRudiFolderWatch;
import de.dfki.mlt.rudibugger.Project.WatchServices.RuleLocationWatch;
import static de.dfki.mlt.rudibugger.Project.WatchServices.RuleLocationWatch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality to track file system changes, e.g. changes
 * in the .rudi folder or a changing RuleLoc.yml file.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class WatchManager {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("Watches");

  /** The <code>DataModel</code> */
  private final DataModel _model;


  /*****************************************************************************
   * WATCH SERVICES
   ****************************************************************************/

  /** Watches the .rudi folder for changes. */
  public RudiFolderWatch rudiWatch;

  /** Watches the RuleLoc.yml file for changes. */
  public RuleLocationWatch ruleLocWatch;


  /*****************************************************************************
   * CONSTRUCTOR
   ****************************************************************************/

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public WatchManager(DataModel model) {
    _model = model;
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /**
   * Initializes WatchServices to monitor changes in the .rudi folder and of the
   * file <code>RuleLoc.yml</code>.
   */
  public void initWatches() {
    ruleLocWatch = createRuleLocationWatch(_model);
    rudiWatch = createRudiFolderWatch(_model);
  }

  /** Disables the project's WatchServices. */
  public void disableWatches() {
    ruleLocWatch.shutDownListener();
    rudiWatch.shutDownListener();
  }


}

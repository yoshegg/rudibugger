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

package de.dfki.mlt.rudibugger.RuleLoggingTableView;

import static de.dfki.mlt.rudibugger.RPC.LogData.BLACK;
import static de.dfki.mlt.rudibugger.RPC.LogData.GRAY;
import static de.dfki.mlt.rudibugger.RPC.LogData.GREEN;
import static de.dfki.mlt.rudibugger.RPC.LogData.RED;
import java.util.HashMap;
import javafx.scene.paint.Color;

/**
 * This class only contains a map indicating the different colours used in the
 * ruleLoggingTableView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ColourMap {

  /** Contains the different colours used to style the log. */
  public static HashMap<Integer, Color> colourMap
          = new HashMap<Integer, Color>() {
    {
      put(RED, Color.RED);
      put(GREEN, Color.GREEN);
      put(GRAY, Color.GRAY);
      put(BLACK, Color.BLACK);
    }
  };

}

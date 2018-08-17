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

package de.dfki.mlt.rudibugger.RPC;

import java.util.Arrays;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.dfki.mlt.rudibugger.project.VondaRuntimeConnection;

/**
 * This API specifies what commands can be sent from VOnDA to rudibugger.
 *
 * The received commands will be parsed an either handed to the correct function
 * or ignored by indicating that they are illegal.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudibuggerAPI implements Consumer<String[]> {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("rudibuggerAPI");

  /** Represents the connection to VOnDA's runtime system. */
  private final VondaRuntimeConnection _vonda;

  /** Initializes the API with the DataModel. */
  public RudibuggerAPI(VondaRuntimeConnection vonda) {
    _vonda = vonda;
  }

  /**
   * Parses incoming commands by dividing them into the command's / function's
   * name and the parameters of the function call. They will then be transmitted
   * to the chosen function or ignored by stating that they are illegal.
   *
   * @param args
   *        Array containing one command and multiple parameters
   */
  public void parseCommand(String[] args) {
    String command = args[0];
    String[] parameters = Arrays.copyOfRange(args, 1, args.length);
    switch (command) {
      case "printLog":
        printLog(parameters);
        break;
      default:
        log.error("Illegal RudibuggerService call: {}",
                Arrays.toString(args));
    }
  }

  @Override
  public void accept(String[] args) { parseCommand(args); }

  /** Starts the process to print a received log in the ruleLogginTableView. */
  public void printLog(String[] args) {
    try {
      int ruleId = Integer.parseInt(args[0]);
      boolean[] result = new boolean[args.length - 1];
      for (int i = 1; i < args.length; ++i) {
        result[i - 1] = Boolean.parseBoolean(args[i]);
      }
      _vonda.printLog(ruleId, result);
    } catch (NumberFormatException ex) {
      log.error("Illegal RudibuggerService Call: "
              + "printLog can't work with parameters {}",
              Arrays.toString(args));
    }
  }

}

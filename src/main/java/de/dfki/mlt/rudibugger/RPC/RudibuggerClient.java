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

import java.io.IOException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.dfki.mlt.rudimant.common.SimpleClient;

/**
 * An XML-RPC Client to connect to rudibugger in Java using VOnDA's
 * SimpleClient. This is manly a wrapper class of this class but includes
 * specific functionality to communicate with VOnDA's runtime system by sending
 * commands to the latter.
 *
 * @author Bernd Kiefer kiefer@dfki.de
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudibuggerClient {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("rudibuggerClient");

  /** An instance of VOnDA's simple client. */
  SimpleClient client;

  /**
   * Initializes a client that connects to the server (VOnDA) on localhost at
   * the given port to send log information to the debugger (rudibugger).
   */
  public RudibuggerClient(String host, int portNumber,
          Consumer<String[]> consumer) {
    client = new SimpleClient(host, portNumber, consumer, "Debugger");
    client.startClient();
  }

  /** @return True, if client is connect to server (VOnDA), else false */
  public boolean isConnected() {
    return client.isConnected();
  }

  /** Disconnects client from server (VOnDA). */
  public void disconnect() throws IOException {
    client.disconnect();
  }

  /**
   * Sets the ruleLoggingState of a given rule to a given state by sending a
   * specific command to VOnDA.
   *
   * @param ruleId
   *        The id of the Rule
   * @param newState
   *        The new state of the given Rule
   */
  public void setLoggingStatus(int ruleId, int newState) {
    client.send("setLogStat", Integer.toString(ruleId), Integer.toString(newState));
  }

  /**
   * Request information from VOnDA about a database entry (HFC).
   *
   * @param uri
   *        The subject of the database entry
   * @param property
   *        The property of the database entry
   *
   */
  public void requestDatabaseInfo(String uri, String property) {
    client.send("reqDbInfo", uri, property);
  }

}

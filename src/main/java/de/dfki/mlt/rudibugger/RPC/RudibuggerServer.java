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

import de.dfki.mlt.rudimant.common.SimpleClient;
import de.dfki.mlt.rudimant.common.SimpleServer;
import java.io.IOException;

/**
 * The server allows vonda to connect to rudibugger and transmit data.
 */
public class RudibuggerServer {

  //private final SimpleClient client;

  private RudibuggerAPI _api;

  /*
  public RudibuggerServer(RudibuggerAPI api, int port) throws IOException {
    _api = api;
    client = new SimpleClient("localhost", port, (args) -> {
      _api.parseCommand(args);
    }, "RudibuggerService");
  }

  public void startServer(int port) {
    client.startServer(port, "RudibuggerService");
  }

  public void stopServer() {
    client.stopServer();
  }*/

}

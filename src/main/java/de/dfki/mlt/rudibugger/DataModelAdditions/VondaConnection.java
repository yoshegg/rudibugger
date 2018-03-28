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
import de.dfki.mlt.rudibugger.RPC.JavaFXLogger;
import de.dfki.mlt.rudibugger.RPC.LogData;
import de.dfki.mlt.rudibugger.RPC.RudibuggerAPI;
import de.dfki.mlt.rudibugger.RPC.RudibuggerClient;
import de.dfki.mlt.rudimant.common.RuleLogger;
import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality to interact with VOnDA.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class VondaConnection {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("vondaConnect");

  /** The <code>DataModel</code> */
  private final DataModel _model;

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public VondaConnection(DataModel model) { _model = model; }

  /** A client that can connect to a server of VOnDA. */
  public RudibuggerClient client;

  /**
   * Establishes connection to VOnDA.
   *
   * (N.B.: The old key for a custom port was <code>SERVER_RUDIMANT</code>, now
   * it is <code>vondaPort</code>.)
   */
  public void connect() {
    int vondaPort = _model.project.getVondaPort();

    client = new RudibuggerClient("localhost", vondaPort,
        new RudibuggerAPI(_model));

    log.debug("RudibuggerClient has been started "
            + "on port [" + vondaPort + "].");
  }

  /** Closes Connection to VOnDA. */
  public void closeConnection() {
    try {
      client.disconnect();
    } catch (IOException e) {
      log.error(e.toString());
    } catch (NullPointerException e) {
      log.info("Could not close connection to VOnDA, "
              + "it was probably never established.");
    }
  }

  /** Represents the connection status between VOnDA and rudibugger. */
  private final BooleanProperty connected = new SimpleBooleanProperty(false);

  /** @return  The connection status property */
  public BooleanProperty connectedProperty() { return connected; }

  /** Represents the most recent logged data. */
  private final ObjectProperty<LogData> logOutput
    = new SimpleObjectProperty<>();

  /** @return  The most recent logged data */
  public ObjectProperty<LogData> logOutputProperty() {
    return logOutput;
  }

  private RuleLogger rl;
  private JavaFXLogger jfl;

  /**
   * Initializes the internal rule logger (just defines how the view should
   * look.
   */
  private void initializeRuleLogger() {
    rl = new RuleLogger();
    rl.setRootInfo(_model.ruleModel.rootImport);
    jfl = new JavaFXLogger();
    rl.registerPrinter(jfl);
    rl.logAllRules();
  }

  /**
   * Intermediate function that transmits data coming from VOnDA to rudibugger.
   *
   * @param ruleId
   * @param result
   */
  public void printLog(int ruleId, boolean[] result) {
    /* Lazy initializing */
    if (rl == null) {
      initializeRuleLogger();
    }

    Platform.runLater(() -> {
      if (!connectedProperty().getValue())
        connectedProperty().setValue(true);
      rl.logRule(ruleId, result);
      if (jfl.pendingLoggingData()) {
        jfl.addRuleIdToLogData(ruleId);
        logOutput.setValue(jfl.popContent());
      }
    });

  }


}

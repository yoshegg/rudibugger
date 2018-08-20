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

package de.dfki.mlt.rudibugger.project;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.project.ruleModel.RuleModel;
import de.dfki.mlt.rudibugger.RPC.*;
import de.dfki.mlt.rudimant.common.RuleLogger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality to interact with VOnDA's runtime system.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class VondaRuntimeConnection {

  static Logger log = LoggerFactory.getLogger("vondaConnect");


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** Represents the project's rule structure. */
  private final RuleModel _ruleModel;

  /** A client that can connect to a server of VOnDA. */
  private RudibuggerClient _client;

  /** TODO What is this? */
  private RuleLogger rl;

  /** TODO What is this? */
  private JavaFXLogger jfl;

  /** Contains every rule's state property and its corresponding listener. */
  private final Map<IntegerProperty, ChangeListener> changeListenerMap
          = new HashMap<>();


  /* ***************************************************************************
   * PROPERTIES AND LISTENERS
   * **************************************************************************/

  /** Represents the connection status between VOnDA and rudibugger. */
  private final IntegerProperty connected
          = new SimpleIntegerProperty(DISCONNECTED_FROM_VONDA);

  /** Represents the most recent logged data. */
  private final ObjectProperty<LogData> mostRecentlogOutput
    = new SimpleObjectProperty<>();


  /**
   * Used to listen to connection state changes. If a connection has been
   * established, changes to RuleInfoExtended objects will be observed.
   */
  private final ChangeListener<Number> connectionStateListener = ((o, ov, nv)
          -> {
    switch (nv.intValue()) {
      case CONNECTED_TO_VONDA:
        setAllLoggingStatuses();
        addListenersForStates();
        log.debug("Connected to VOnDA.");
        break;
      case ESTABLISHING_CONNECTION:
        log.debug("Establishing connection to VOnDA...");
        break;
      case DISCONNECTED_FROM_VONDA:
        removeListenersForStates();
        log.debug("Disconnected from VOnDA.");
    }
  });


  /*****************************************************************************
   * INITIALIZERS, CONNECT AND DISCONNECT
   ****************************************************************************/

  /**
   * Initializes this addition of <code>DataModel</code>.
   * TODO
//   * @param model  The current <code>DataModel</code>
   */
  public VondaRuntimeConnection(RuleModel ruleModel) { _ruleModel = ruleModel; }

  /**
   * Initializes the internal rule logger (just defines how the view should
   * look.
   */
  private void initializeRuleLogger() {
    rl = new RuleLogger();
    rl.setRootInfo(_ruleModel.getRootImport());
    jfl = new JavaFXLogger();
    rl.registerPrinter(jfl);
    rl.logAllRules();
  }

 /**
   * Establishes connection to VOnDA.
   *
   * (N.B.: The old key for a custom port was <code>SERVER_RUDIMANT</code>, now
   * it is <code>vondaPort</code>.)
   */
  public void connect(int vondaPort) {
    _client = new RudibuggerClient("localhost", vondaPort,
        new RudibuggerAPI(this));

    connected.set(ESTABLISHING_CONNECTION);
    connected.addListener(connectionStateListener);

    log.debug("RudibuggerClient has been started "
            + "on port [" + vondaPort + "].");
  }

  /** Closes Connection to VOnDA. */
  public void closeConnection() {
    try {
      _client.disconnect();
    } catch (IOException e) {
      log.error(e.toString());
    } catch (NullPointerException e) {
      log.info("Could not close connection to VOnDA, "
              + "it was probably never established.");
    }

    connected.set(DISCONNECTED_FROM_VONDA);
    connected.removeListener(connectionStateListener);

    log.debug("RudibuggerClient has been shut down.");
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Sends the loggingStatus of all rules to VOnDA. */
  private void setAllLoggingStatuses() {
    _ruleModel.idLoggingStatesMap().keySet().forEach((ruleId) -> {
      int loggingState = _ruleModel.idLoggingStatesMap()
              .get(ruleId).get();
      setLoggingStatus(ruleId, loggingState);
    });
  }

  /**
   * Sets the logging state of a given rule.
   *
   * @param id
   * @param value
   */
  private void setLoggingStatus(int id, int value) {
    if ((_client != null) && (_client.isConnected()))
      _client.setLoggingStatus(id, value);
  }

  /**
   * Creates a new listener to track a changing state of a given rule. If a
   * change occurred, this change will be sent to VOnDA immediately
   *
   * @param ruleId The id of the wanted rule.
   */
  private ChangeListener<Number> createRuleStateListener(int ruleId) {
    ChangeListener<Number> cl = (o, ov, nv) ->
      setLoggingStatus(ruleId, nv.intValue());
    return cl;
  }

  /** Add listeners to every rule's state property */
  private void addListenersForStates() {
    ObservableMap<Integer, IntegerProperty> map
            = _ruleModel.idLoggingStatesMap();
    map.keySet().forEach((ruleId) -> {
      ChangeListener<Number> cl = createRuleStateListener(ruleId);
      IntegerProperty prop = map.get(ruleId);
      prop.addListener(cl);
      changeListenerMap.put(prop, cl);
    });
  }

  /** Remove every listener for state changes from every rule. */
  private void removeListenersForStates() {
    changeListenerMap.keySet().forEach((prop) ->
      prop.removeListener(changeListenerMap.get(prop)));
    changeListenerMap.clear();
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
      if (connectedProperty().getValue() != CONNECTED_TO_VONDA)
        connectedProperty().setValue(CONNECTED_TO_VONDA);
      rl.logRule(ruleId, result);
      if (jfl.pendingLoggingData()) {
        jfl.addRuleIdToLogData(ruleId);
        mostRecentlogOutput.setValue(jfl.popContent());
      }
    });

  }


  /*****************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS AND PROPERTIES
   ****************************************************************************/

  /** @return The most recent logged data */
  public ObjectProperty<LogData> logOutputProperty() {
    return mostRecentlogOutput;
  }

  /** @return The connection status property */
  public IntegerProperty connectedProperty() { return connected; }

  /** @return The connection state */
  public int getConnectionState() { return connected.get(); }

}

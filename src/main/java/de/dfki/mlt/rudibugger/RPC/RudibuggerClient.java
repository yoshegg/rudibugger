package de.dfki.mlt.rudibugger.RPC;

import java.io.IOException;

import de.dfki.mlt.rudimant.common.SimpleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * an XML-RPC Client to connect to the debugger in Java
 */
public class RudibuggerClient {

  /** The logger of the the RuleModel */
  static Logger log = LoggerFactory.getLogger("vonda");

  SimpleClient client;

  /** A client that connects to the server on localhost at the given port to
   *  send log information to the debugger.
   */
  public RudibuggerClient(int portNumber) {
    client = new SimpleClient(portNumber);
  }

  public void disconnect() throws IOException {
    client.disconnect();
  }

  public void setLoggingStatus(int ruleId, int what) {
    try {
      client.send(ruleId, what);
    } catch (IOException e) {
      log.error("Could not set logging status of rule " + ruleId + "\n" + e);
    }
  }

}

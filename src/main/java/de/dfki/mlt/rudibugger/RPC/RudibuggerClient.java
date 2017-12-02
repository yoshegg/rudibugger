package de.dfki.mlt.rudibugger.RPC;

import java.io.IOException;

import de.dfki.mlt.rudimant.common.SimpleClient;
import org.apache.log4j.Logger;

/**
 * an XML-RPC Client to connect to the debugger in Java
 */
public class RudibuggerClient {

  /** The logger of the the RuleModel */
  static Logger log = Logger.getLogger("vonda");

  SimpleClient client;

  /** A client that connects to the server on localhost at the given port to
   *  send log information to the debugger.
   */
  public RudibuggerClient(int portNumber) {
    client = new SimpleClient(portNumber);
  }

  public void setLoggingStatus(int ruleId, int what) {
    try {
      client.send(ruleId, what);
    } catch (IOException e) {
      log.error("Could not set logging status of rule " + ruleId + "\n" + e);
    }
  }

}

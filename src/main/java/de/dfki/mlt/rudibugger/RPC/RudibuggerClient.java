package de.dfki.mlt.rudibugger.RPC;

import java.io.IOException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.mlt.rudimant.common.SimpleClient;
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
  public RudibuggerClient(String host, int portNumber,
      Consumer<String[]> consumer) {
    client = new SimpleClient(host, portNumber, consumer, "Debugger");
    client.startClient();
  }

  public boolean isConnected() {
    return client.isConnected();
  }

  public void disconnect() throws IOException {
    client.disconnect();
  }

  public void setLoggingStatus(int ruleId, int what) {
    client.send(Integer.toString(ruleId), Integer.toString(what));
  }

}

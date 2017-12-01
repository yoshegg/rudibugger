package de.dfki.mlt.rudibugger.RPC;

import java.io.IOException;

import de.dfki.mlt.rudimant.common.SimpleClient;

/**
 * an XML-RPC Client to connect to the debugger in Java
 */
public class RudibuggerClient {

  SimpleClient client;

  /** A client that connects to the server on localhost at the given port to
   *  send log information to the debugger.
   */
  public RudibuggerClient(int portNumber) {
    client = new SimpleClient(portNumber);
  }

  public void setLoggingStatus(int ruleId, int what) throws IOException {
    client.send(ruleId, what);
  }

}

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

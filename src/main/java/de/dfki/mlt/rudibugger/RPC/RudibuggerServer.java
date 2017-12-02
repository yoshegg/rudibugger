package de.dfki.mlt.rudibugger.RPC;

import de.dfki.mlt.rudimant.common.SimpleServer;
import java.io.IOException;

/**
 * The server allows vonda to connect to rudibugger and transmit data.
 */
public class RudibuggerServer {

  private final SimpleServer server;

  private RudibuggerAPI _api;

  public RudibuggerServer(RudibuggerAPI api) throws IOException {
    _api = api;
    server = new SimpleServer((String[] args) -> {
      _api.parseCommand(args);
    });
  }

  public void startServer(int port) {
    server.startServer(port, "RudibuggerService");
  }

}

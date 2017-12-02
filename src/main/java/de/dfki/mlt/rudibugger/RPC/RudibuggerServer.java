package de.dfki.mlt.rudibugger.RPC;

import de.dfki.mlt.rudimant.common.SimpleServer;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class RudibuggerServer {

  private static Logger logger = LoggerFactory.getLogger(RudibuggerServer.class);

  private SimpleServer server;

  private RudibuggerAPI _api;

  public RudibuggerServer(RudibuggerAPI api) throws IOException {
    _api = api;
    server = new SimpleServer((String[] args) -> {
      _api.parseCommand(args);
    });
  }

  public void startServer(int port) {
    server.startServer(port, "DebuggingService");
  }

}

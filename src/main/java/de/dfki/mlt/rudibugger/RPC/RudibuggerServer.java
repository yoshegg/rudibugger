package de.dfki.mlt.rudibugger.RPC;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.mlt.rudimant.common.SimpleServer;

/**
 */
public class RudibuggerServer {

  private static Logger logger = LoggerFactory.getLogger(RudibuggerServer.class);

  private SimpleServer server;

  public RudibuggerServer(Object connectionToYourWindow) throws IOException {
    server = new SimpleServer(new SimpleServer.Callable() {
      @Override
      public void execute(String[] args) {
        try {
          int ruleId = Integer.parseInt(args[0]);
          boolean[] result = new boolean[args.length - 1];
          for (int i = 1; i < args.length; ++i) {
            result[i - 1] = Boolean.parseBoolean(args[i]);
          }
          // connectionToYourWindow.printLog(ruleId, result)
          System.out.println(Arrays.toString(args));
        } catch (Throwable ex) {
          logger.error("Illegal RudibuggerService Call: {}",
              Arrays.toString(args));
        }
      }
    });
  }

  public void startServer(int port) {
    server.startServer(port, "DebuggingService");
  }

}

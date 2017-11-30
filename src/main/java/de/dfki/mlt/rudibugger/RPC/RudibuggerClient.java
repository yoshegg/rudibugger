package de.dfki.mlt.rudibugger.RPC;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * an XML-RPC Client to connect to the debugger in Java
 */
public class RudibuggerClient {

  private static Logger logger = LoggerFactory.getLogger(RudibuggerClient.class);

  private Socket socket;

  public static String hostName = "localhost";

  private OutputStreamWriter out;

  /** A client that connects to the server on localhost at the given port to
   *  send log information to the debugger.
   */
  public RudibuggerClient(int portNumber) {
    try {
      socket = new Socket(hostName, portNumber);
      out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
    } catch (UnknownHostException e) {
      logger.error("Unkown host {}: {}", hostName, e);
    } catch (IOException e) {
      logger.error("IOException {}: {}", hostName, e);
    }
  }

  public void setLoggingStatus(int ruleId, int what) {
    try {
      out.write(Integer.toString(ruleId));
      out.write(";");
      out.write(Integer.toString(what));
      out.write("\t");
      out.flush();
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }

}

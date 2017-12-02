/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RPC;

import de.dfki.mlt.rudibugger.DataModel;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudibuggerAPI {

  /** the logger */
  static Logger log = LoggerFactory.getLogger("rudibuggerAPI");

  DataModel _model;

  public RudibuggerAPI(DataModel model) {
    _model = model;
  }

  public void parseCommand(String[] args) {
    String command = args[0];
    String[] parameters = Arrays.copyOfRange(args, 1, args.length-1);
    switch (command) {
      case "printLog":
        printLog(parameters);
        break;
      default:
        log.error("Illegal RudibuggerService call: {}",
                Arrays.toString(args));
    }
  }

  public void printLog(String[] args) {
    try {
      int ruleId = Integer.parseInt(args[0]);
      boolean[] result = new boolean[args.length - 1];
      for (int i = 1; i < args.length; ++i) {
        result[i - 1] = Boolean.parseBoolean(args[i]);
      }
      // _model.printLog(ruleId, result)
    } catch (NumberFormatException ex) {
      log.error("Illegal RudibuggerService Call: "
              + "printLog can't work with parameters {}",
              Arrays.toString(args));
    }
  }

}

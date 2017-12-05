/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RPC;

import de.dfki.mlt.rudimant.common.DefaultLogger;
import static de.dfki.mlt.rudibugger.RPC.LogData.*;
import java.util.ArrayList;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class JavaFXLogger extends DefaultLogger {

  private ArrayList<LogData> data
    = new ArrayList<LogData>() {{add(new LogData());}};

  private void printInColor(String s, int color) {
    data.get(0).addStringPart(s, color);
  }

  public LogData popContent() {
    LogData returnVal = data.remove(0);
    data.add(new LogData());
    return returnVal;
  }

  @Override
  protected void printTerm(String term, boolean value, boolean shortCut) {
    printInColor(term, shortCut ? GRAY : value ? GREEN : RED);
  }

  @Override
  protected void printResult(String label, boolean value) {
    printInColor(label + ": ", value ? GREEN : RED);
  }

}

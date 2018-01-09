/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RPC;

import java.util.ArrayList;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class LogData {

  protected static final int RED = 1;
  protected static final int GREEN = 2;
  protected static final int GRAY = 3;
  protected static final int BLACK = 0;

  protected class StringPart {
    public String content;
    public int colour;

    private StringPart(String content, int colour)  {
      this.content = content;
      this.colour = colour;
    }
  }

  public ArrayList<StringPart> text = new ArrayList<>();

  public void addStringPart(String content, int colour) {
    text.add(new StringPart(content, colour));
  }


}

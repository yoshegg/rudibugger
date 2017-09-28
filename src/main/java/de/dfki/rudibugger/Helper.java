/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger;

import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Helper {

  static Logger log = Logger.getLogger("rudiLog");

  // taken from https://stackoverflow.com/questions/17307761/is-there-a-java-equivalent-to-pythons-easy-string-splicing

  public static String slice_start(String s, int startIndex) {
    if (startIndex < 0) {
      startIndex = s.length() + startIndex;
    }
    return s.substring(startIndex);
  }

  public static String slice_end(String s, int endIndex) {
    if (endIndex < 0) {
      endIndex = s.length() + endIndex;
    }
    return s.substring(0, endIndex);
  }

  public static String slice_range(String s, int startIndex, int endIndex) {
    if (startIndex < 0) {
      startIndex = s.length() + startIndex;
    }
    if (endIndex < 0) {
      endIndex = s.length() + endIndex;
    }
    return s.substring(startIndex, endIndex);
  }
}

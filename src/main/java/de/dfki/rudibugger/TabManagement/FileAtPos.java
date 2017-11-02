/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.TabManagement;

import java.nio.file.Path;

/**
 * This class is used to transmit a request for a file from one controller to
 * another via the model.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class FileAtPos {

  private final Path _file;
  private final int _pos;

  public Path getFile() {
    return _file;
  }

  public int getPosition() {
    return _pos;
  }

  public FileAtPos(Path path, Integer position) {
    _file = path;
    _pos = position;
  }

}

/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RudiList;

import java.nio.file.Path;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import static de.dfki.rudibugger.Constants.*;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiPath {

  private Path _path;

  private IntegerProperty _used = new SimpleIntegerProperty(FILE_NOT_USED);
  public IntegerProperty _usedProperty() { return _used; }

  public RudiPath(Path path) {
    _path = path;
  }

  public Path getPath() {
    return _path;
  }

  @Override
  public String toString() {
    return _path.getFileName().toString();
  }

}

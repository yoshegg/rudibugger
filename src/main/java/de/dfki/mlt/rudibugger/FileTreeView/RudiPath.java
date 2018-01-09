/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.FileTreeView;

import java.nio.file.Path;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import static de.dfki.mlt.rudibugger.Constants.*;
import java.nio.file.Files;
import java.util.Objects;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiPath {

  private final Path _path;

  private final IntegerProperty _used;
  public IntegerProperty _usedProperty() { return _used; }

  public RudiPath(Path path) {
    _path = path;
    if (Files.isDirectory(path)) {
      _used = new SimpleIntegerProperty(IS_FOLDER);
    } else {
      _used = new SimpleIntegerProperty(FILE_NOT_USED);
    }
  }

  public Path getPath() {
    return _path;
  }

  @Override
  public String toString() {
    return _path.getFileName().toString();
  }

  @Override
  public boolean equals(Object obj) {
		if (!(obj instanceof RudiPath))
			return false;
		if (obj == this)
			return true;
		return this._path.equals(((RudiPath) obj)._path);
	}

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + Objects.hashCode(this._path);
    return hash;
  }

}

/*
 * The Creative Commons CC-BY-NC 4.0 License
 *
 * http://creativecommons.org/licenses/by-nc/4.0/legalcode
 *
 * Creative Commons (CC) by DFKI GmbH
 *  - Bernd Kiefer <kiefer@dfki.de>
 *  - Anna Welker <anna.welker@dfki.de>
 *  - Christophe Biwer <christophe.biwer@dfki.de>
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.dfki.mlt.rudibugger.FileTreeView;

import java.nio.file.Path;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import static de.dfki.mlt.rudibugger.Constants.*;
import java.nio.file.Files;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiPath {

  private final Path _path;

  private final IntegerProperty _used;
  public IntegerProperty _usedProperty() { return _used; }

  private final BooleanProperty _modified;
  public BooleanProperty _modifiedProperty() { return _modified; }

  public RudiPath(Path path) {
    _path = path;
    if (Files.isDirectory(path)) {
      _used = new SimpleIntegerProperty(IS_FOLDER);
    } else {
      _used = new SimpleIntegerProperty(FILE_NOT_USED);
    }
    _modified = new SimpleBooleanProperty(false);
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

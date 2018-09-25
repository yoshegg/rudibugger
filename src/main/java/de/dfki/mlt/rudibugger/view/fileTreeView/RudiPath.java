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

package de.dfki.mlt.rudibugger.view.fileTreeView;

import java.nio.file.Path;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.project.ruleModel.ImportInfoExtended;
import java.nio.file.Files;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * This class is some kind of extension to the usual <code>Path</code> class. It
 * contains a <code>Path</code> field, but also properties indicating whether or
 * not a file has been used in the current compiled project and whether or not
 * it has been modified since the last compilation attempt.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiPath {

  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** Represents the path of the associated <code>.rudi</code> file. */
  private final Path _path;

  /** Indicates the usage state of the associated <code>.rudi</code> file. */
  private final IntegerProperty _used;

  /**
   * Indicates whether or not the associated <code>.rudi</code> file has been
   * modified since the last successful compilation.
   */
  private final BooleanProperty _modified;

  /** Represents the associated ImportInfoExtended in the RuleModel. */
  private ImportInfoExtended _importInfo;


  /* ***************************************************************************
   * CONSTRUCTOR
   * **************************************************************************/

  /**
   * Creates a new <code>RudiPath</code> object.
   *
   * @param path
   *        The associated <code>.rudi</code> file
   */
  public RudiPath(Path path) {
    _path = path;
    if (Files.isDirectory(path)) {
      _used = new SimpleIntegerProperty(IS_FOLDER);
    } else {
      _used = new SimpleIntegerProperty(FILE_NOT_USED);
    }
    _modified = new SimpleBooleanProperty(false);
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

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


  /* ***************************************************************************
   * SETTERS
   * **************************************************************************/

  /**
   * Links a given <code>ImportInfoExtended</code> to this
   * <code>RudiPath</code>.
   *
   * @param ii The associated <code>ImportInfoExtended</code>
   */
  public void setImportInfo(ImportInfoExtended ii) { _importInfo = ii; }


  /* ***************************************************************************
   * GETTERS
   * **************************************************************************/

  /** @return The associated <code>ImportInfoExtended</code> */
  public ImportInfoExtended getImportInfo() { return _importInfo; }

  /** @return The <code>Path</code> of the associated <code>.rudi</code> file */
  public Path getPath() { return _path; }

  /**
   * @return Indicates if the associated <code>.rudi</code> file has been
   * modified since the last successful compilation
   */
  public BooleanProperty modifiedProperty() { return _modified; }

  /**
   * @return Indicates the usage state of the associated <code>.rudi</code>
   * file
   */
  public IntegerProperty usedProperty() { return _used; }

}

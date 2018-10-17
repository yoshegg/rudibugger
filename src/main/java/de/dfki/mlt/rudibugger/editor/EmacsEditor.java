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
package de.dfki.mlt.rudibugger.editor;

import de.dfki.mlt.rudibugger.DataModel;
import java.nio.file.Path;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class EmacsEditor extends Editor {

  private final EmacsConnection _emacs;

  
  /* ***************************************************************************
   * CONSTRUCTOR & OTHER METHODS
   * **************************************************************************/

  public static EmacsEditor getNewEditor(DataModel model) {
    return new EmacsEditor(model);
  }

  private EmacsEditor(DataModel model) {
    _emacs = new EmacsConnection(model);
  }


  public static void closeEditor(EmacsEditor re) {
    re.closeAllFiles();
  }




  @Override
  public void saveFile() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void saveFileAs(Path file) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void saveAllFiles() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void createNewFile() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void loadFile(Path file) {
    if (! _emacs.isAlive()) {
      _emacs.startConnection("emacs");
    }
    _emacs.getConnector().visitFilePosition(file.toFile(), 1, 0, "");
  }

  @Override
  public void loadFileAtLine(Path file, int line) {
    if (! _emacs.isAlive()) {
      _emacs.startConnection("emacs");
    }
    _emacs.getConnector().visitFilePosition(file.toFile(), line, 0, "");
  }

  @Override
  public void closeFile() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void closeAllFiles() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}

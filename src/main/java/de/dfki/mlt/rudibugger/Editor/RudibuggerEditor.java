package de.dfki.mlt.rudibugger.Editor;

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

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudibuggerEditor extends Editor {


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  // currently open files

  // currently open file



  /* ***************************************************************************
   * CONSTRUCTOR
   * **************************************************************************/

  @Override
  public RudibuggerEditor getNewEditor() {
    return new RudibuggerEditor();
  }

  private RudibuggerEditor() {}


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  @Override
  public void saveFile() {

  }

  @Override
  public void saveAllFiles() {

  }

  @Override
  public void loadFile() {

  }

  @Override
  public void closeEditor() {

  }

  @Override
  public void closeAllFiles() {

  }

  @Override
  public void loadLine() {

  }

}

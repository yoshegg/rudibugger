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

import java.nio.file.Path;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public abstract class Editor {

  /* ***************************************************************************
   * SAVE METHODS
   * **************************************************************************/

  public abstract void saveFile();

  public abstract void saveFileAs(Path file);

  public abstract void saveAllFiles();


  /* ***************************************************************************
   * LOAD METHODS
   * **************************************************************************/

  public abstract void createNewFile();

  public abstract void loadFile(Path file);

  public abstract void loadFileAtLine(Path file, int line);


  /* ***************************************************************************
   * CLOSE METHODS
   * **************************************************************************/

  public abstract void closeFile();

  public abstract void closeAllFiles();

}

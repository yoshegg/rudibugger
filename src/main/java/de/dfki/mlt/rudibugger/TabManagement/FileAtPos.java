///*
// * The Creative Commons CC-BY-NC 4.0 License
// *
// * http://creativecommons.org/licenses/by-nc/4.0/legalcode
// *
// * Creative Commons (CC) by DFKI GmbH
// *  - Bernd Kiefer <kiefer@dfki.de>
// *  - Anna Welker <anna.welker@dfki.de>
// *  - Christophe Biwer <christophe.biwer@dfki.de>
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// * IN THE SOFTWARE.
// */
//
//package de.dfki.mlt.rudibugger.TabManagement;
//
//import java.nio.file.Path;
//
///**
// * This class is used to transmit a request for a file from one controller to
// * another via the model.
// *
// * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
// */
//public class FileAtPos {
//
//  /** Represents the requested file. */
//  private final Path _file;
//
//  /** Represents the requested line of the requested file. */
//  private final int _pos;
//
//  /**
//   * Creates a new request.
//   *
//   * @param path
//   *        The requested file.
//   * @param position
//   *        The line of the requested file.
//   */
//  public FileAtPos(Path path, Integer position) {
//    _file = path;
//    _pos = position;
//  }
//
//  /** @return The requested file. */
//  public Path getFile() { return _file; }
//
//  /** @return The requested line of the requested file. */
//  public int getPosition() { return _pos; }
//
//}

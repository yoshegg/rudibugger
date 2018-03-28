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

package de.dfki.mlt.rudibugger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Helper {

  static Logger log = LoggerFactory.getLogger("rudiLog");

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

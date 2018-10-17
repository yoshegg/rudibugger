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

package de.dfki.mlt.rudibugger.view.ruleLoggingTableView;

import de.dfki.mlt.rudibugger.rpc.LogData;
import de.dfki.mlt.rudibugger.rpc.LogData.DatePart;
import java.text.SimpleDateFormat;
import javafx.scene.control.TableCell;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * This TableCell is responsible for the appearance of the leftmost column of
 * the ruleLoggingTableView which contains the timestamp indicating when this
 * rule has been traversed / logged.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class TimestampCellFactory extends TableCell<LogData, DatePart> {

  /**
   * Defines the look of this cell.
   *
   * @param timeStampIndex
   *        Defines if an index should be shown alongside the timestamp
   */
  public TimestampCellFactory(boolean timeStampIndex) {
    _timeStampIndex = timeStampIndex;
  }

  /**
   * Defines whether or not an index should be shown alongside the timestamp.
   * This is especially useful when a lot of rules are logged and some of them
   * are executed at the exact same millisecond.
   */
  private final boolean _timeStampIndex;

  /** Represents the current date at the time of creation. */
  public static SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss.SSS");

  @Override
    protected void updateItem(DatePart item, boolean empty) {
      super.updateItem(item, empty);

      if (empty || item == null) {
        setText(null);
        setGraphic(null);
      } else {
        TextFlow textFlow = new TextFlow();
        Text t;
        if (_timeStampIndex) {
          t = new Text(dt.format(item.date) + " / " + item.counter);
        } else {
          t = new Text(dt.format(item.date));
        }
        textFlow.getChildren().add(t);
        setGraphic(textFlow);
      }
    }
    
  }

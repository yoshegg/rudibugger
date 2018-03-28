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

package de.dfki.mlt.rudibugger.RPC;

import static de.dfki.mlt.rudibugger.RPC.LogData.*;
import de.dfki.mlt.rudibugger.RPC.LogData.DatePart;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class TimestampCellFactory extends TableCell<LogData, DatePart> {

  public TimestampCellFactory(boolean timeStampIndex) {
    _timeStampIndex = timeStampIndex;
  }

  private boolean _timeStampIndex;

  public static HashMap<Integer, Color> colourMap
          = new HashMap<Integer, Color>() {{
      put(RED, Color.RED);
      put(GREEN, Color.GREEN);
      put(GRAY, Color.GRAY);
      put(BLACK, Color.BLACK);
    }};

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

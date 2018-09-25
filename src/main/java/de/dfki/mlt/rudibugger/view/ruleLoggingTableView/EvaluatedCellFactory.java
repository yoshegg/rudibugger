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
import de.dfki.mlt.rudibugger.rpc.LogData.StringPart;
import static de.dfki.mlt.rudibugger.view.ruleLoggingTableView.ColourMap.colourMap;
import java.util.ArrayList;
import javafx.scene.control.TableCell;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * This TableCell is responsible for the appearance of the rightmost column of
 * the ruleLoggingTableView which contains the content of the rule itself
 * and what parts of it were evaluated to which state.
 *
 * This is where the short-cut logic will be visually shown as it clearly
 * indicates what part of the rule have not been evaluated.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class EvaluatedCellFactory
        extends TableCell<LogData, ArrayList<StringPart>> {

  @Override
  protected void updateItem(ArrayList<LogData.StringPart> item, boolean empty) {
    super.updateItem(item, empty);

    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      TextFlow textFlow = new TextFlow();
      for (LogData.StringPart x : item) {
        Text t = new Text(x.content);
        t.setFill(colourMap.get(x.evalOutcome));
        textFlow.getChildren().add(t);
      }
      setGraphic(textFlow);
    }
  }

}

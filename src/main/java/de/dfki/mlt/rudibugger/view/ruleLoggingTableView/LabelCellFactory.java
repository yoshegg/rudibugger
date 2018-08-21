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
import static de.dfki.mlt.rudibugger.view.ruleLoggingTableView.ColourMap.colourMap;
import javafx.scene.control.TableCell;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * This TableCell is responsible for the appearance of the middle column of
 * the ruleLoggingTableView which contains the rule's label.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class LabelCellFactory extends TableCell<LogData, LogData.StringPart> {

  @Override
  protected void updateItem(LogData.StringPart item, boolean empty) {
    super.updateItem(item, empty);

    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      TextFlow textFlow = new TextFlow();
      Text t = new Text(item.content);
        t.setFill(colourMap.get(item.evalOutcome));
        textFlow.getChildren().add(t);
        setGraphic(textFlow);
      }
    }
  
  }
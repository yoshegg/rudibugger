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

import de.dfki.mlt.rudimant.common.DefaultLogger;
import static de.dfki.mlt.rudibugger.RPC.LogData.*;
import java.util.ArrayList;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class JavaFXLogger extends DefaultLogger {

  private ArrayList<LogData> data = new ArrayList<LogData>();

  private void printInColor(String s, int color) {
    if (data.isEmpty()) data.add(new LogData());
    data.get(0).addStringPart(s, color);
  }

  public LogData popContent() {
    LogData returnVal = data.remove(0);
    return returnVal;
  }

  public void addRuleIdToLogData(int ruleId) {
    data.get(0).addRuleId(ruleId);
  }

  public boolean pendingLoggingData() {
    return ! data.isEmpty();
  }

  @Override
  protected void print(String s) {
    if (s != "\n")
      data.get(0).addStringPart(s, BLACK);
  }

  @Override
  protected void printTerm(String term, boolean value, boolean shortCut) {
    printInColor(term, shortCut ? GRAY : value ? GREEN : RED);
  }

  @Override
  protected void printResult(String label, boolean value) {
    printInColor(label, value ? GREEN : RED);
  }

}

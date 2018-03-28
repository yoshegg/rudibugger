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

import java.util.ArrayList;
import java.util.Date;
import javafx.beans.property.SimpleObjectProperty;

/**
 * This class is used to organize the sent data by vonda. Its fields will be
 * used to show the needed information in the ruleLoggingTableView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class LogData {

  /* The used colours */
  protected static final int RED = 1;
  protected static final int GREEN = 2;
  protected static final int GRAY = 3;
  protected static final int BLACK = 0;

  /** The last used timestamp */
  private static Date currentDate = new Date();

  /** The number of times the last used timestamp has been used */
  private static int timeCounter = 1;

  /** This container class contains a String and its respective colour */
  public class StringPart {

    public String content;
    public int colour;

    private StringPart(String content, int colour)  {
      this.content = content;
      this.colour = colour;
    }
  }

  /**
   * This container class contains the time of logging and the ranking of that
   * exact timestamp. This is necessary if things are logged at the exact same
   * moment.
   */
  public class DatePart {

    public Date date;
    public int counter;

    private DatePart(Date date, int counter) {
      this.date = date;
      this.counter = counter;
    }
  }

  /** The constructor of LogData */
  public LogData() {
    Date date = new Date();
    if (currentDate.equals(date)) {
      timeCounter++;
    } else {
      currentDate = date;
      timeCounter = 1;
    }
      timestamp = new SimpleObjectProperty<>(new DatePart(date, timeCounter));
  }

  /** The label of this LogData */
  public SimpleObjectProperty<StringPart> label
          = new SimpleObjectProperty<>();

  /** The evaluated data of this LogData */
  public SimpleObjectProperty<ArrayList<StringPart>> evaluated
          = new SimpleObjectProperty<>(new ArrayList<>());

  /** The timestamp of this LogData */
  public SimpleObjectProperty<DatePart> timestamp;

  /** The ruleId of this LogData */
  private int _ruleId;

  /** This method is called to add another String and its colour to LogData */
  public void addStringPart(String content, int colour) {
    if (label.getValue() == null) {
      label.setValue(new StringPart(content, colour));
    } else {
      evaluated.getValue().add(new StringPart(content, colour));
    }
  }

  /**
   * This method adds the ruleId to the instance of LogData. This is necessary
   * due to restrictions of the superclasses of the Logger classes.
   *
   * @param ruleId
   */
  public void addRuleId(int ruleId) {
    _ruleId = ruleId;
  }

  /**
   * Getter for the ruleId
   *
   * @return
   */
  public int getRuleId() {
    return _ruleId;
  }

}

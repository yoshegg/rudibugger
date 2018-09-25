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

package de.dfki.mlt.rudibugger.rpc;

import java.util.ArrayList;
import java.util.Date;
import javafx.beans.property.SimpleObjectProperty;

/**
 * This class is used to organize the received data from VOnDA. One instance of
 * this class represents one log entry. Its fields will
 * be used as columns in the different rows of the ruleLoggingTreeView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class LogData {


  /* ***************************************************************************
   * EVALUATION OUTCOMES
   * **************************************************************************/

  /** Indicates that something evaluated to false. */
  public static final int RED = 1;

  /** Indicates that something evaluated to true. */
  public static final int GREEN = 2;

  /** Indicates that something has not been evaluated, but could have been. */
  public static final int GRAY = 3;

  /** Indicates that something has not been evaluated, and should not be. */
  public static final int BLACK = 0;


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** Currently used timestamp. */
  private static Date currentDate = new Date();

  /**
   * Represents when the specific rule was logged and how many rules where
   * logged at the exact same millisecond + 1.
   */
  public SimpleObjectProperty<DatePart> timestamp;

  /** Indicates how often the current timestamp has been used. */
  private static int timeCounter = 1;

  /** Represents the evaluated rule's label. */
  public SimpleObjectProperty<StringPart> label = new SimpleObjectProperty<>();

  /** Represents the evaluated strings / rule parts of the logged rule. */
  public SimpleObjectProperty<ArrayList<StringPart>> evaluatedRuleParts
          = new SimpleObjectProperty<>(new ArrayList<>());

  /** Represents the ruleId of the logged rule. */
  private int _ruleId;


  /* ***************************************************************************
   * SUBCLASSES
   * **************************************************************************/

  /**
   * Container class represents a (usually short) String and its evaluation
   * state.
   */
  public class StringPart {

    /** Represents a (short) String) */
    public String content;

    /** Represents the evaluation outcome of the included String. */
    public int evalOutcome;

    /**
     * Creates a new instance of the container class StringPart.
     *
     * @param content
     *        A (small) String, a part of a rule
     * @param evalOutcome
     *        The evaluation outcome of the given rule part
     *
     */
    private StringPart(String content, int evalOutcome)  {
      this.content = content;
      this.evalOutcome = evalOutcome;
    }

  }

  /**
   * Container class contains the time of logging and the ranking of that exact
   * timestamp. The ranking is necessary if things are logged at the exact same
   * moment.
   */
  public class DatePart {

    /** Represents the date. */
    public Date date;

    /** Represents the number of times something occurred at that exact time. */
    public int counter;

    /**
     * Creates a new instance of the container class DatePart.
     *
     * @param date
     *        The current date
     * @param counter
     *        The number of times something occurred at the given date
     */
    private DatePart(Date date, int counter) {
      this.date = date;
      this.counter = counter;
    }

  }


  /* ***************************************************************************
   * CLASS-SPECIFIC METHODS
   * **************************************************************************/

  /** Creates a new LogData object. */
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

  /**
   * Adds a part of the rule to the evaluated rule parts.
   *
   * @param content
   *        A part of a rule
   * @param evalOutcome
   *        The evaluation outcome of the given part of the rule
   */
  public void addStringPart(String content, int evalOutcome) {
    if (label.getValue() == null)
      label.setValue(new StringPart(content, evalOutcome));
    else
      evaluatedRuleParts.getValue().add(new StringPart(content, evalOutcome));
  }

  /**
   * Adds the ruleId to the instance of LogData.
   *
   * <br/><br/>(<i>This function is necessary due to restrictions of the
   * superclasses of the Logger classes.</i>)
   *
   * @param ruleId
   *        The ruleId of the given rule
   */
  public void addRuleId(int ruleId) { _ruleId = ruleId; }

  /** @return The ruleId of the rule linked to this log entry. */
  public int getRuleId() { return _ruleId; }

}

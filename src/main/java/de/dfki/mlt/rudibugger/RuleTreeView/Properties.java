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

package de.dfki.mlt.rudibugger.RuleTreeView;

/**
 * Represents the current state of a TreeItem: Is it expanded, what is its
 * logging state and is it an import.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Properties {

  /*****************************************************************************
   * FIELDS
   ****************************************************************************/

  /** Represents the expansion state of the associated TreeItem. */
  Boolean isExpanded;

  /** Represents the ruleLoggingState of the associated TreeItem. */
  Integer loggingState;

  /** Used to save whether or not the associated TreeItem is an Import. */
  Boolean isImport = false;


  /*****************************************************************************
   * CONSTRUCTOR
   ****************************************************************************/

  /**
   * Creates a new Properties instance.
   *
   * @param expStat True, if associated TreeItem is expanded, else false
   * @param logStat An Integer representing the ruleLoggingState
   */
  public Properties(Boolean expStat, int logStat) {
    isExpanded = expStat;
    loggingState = logStat;
  }


  /*****************************************************************************
   * GETTER / SETTER
   ****************************************************************************/

  /** @return True, if associated TreeItem is expanded, else false */
  public Boolean getIsExpanded() {
    return isExpanded;
  }

  /**
   * Sets expansion state of associated TreeItem.
   *
   * @param isExpanded  True, if associated TreeItem is expanded, else false
   */
  public void setIsExpanded(Boolean isExpanded) {
    this.isExpanded = isExpanded;
  }

  /** @return An Integer representing the ruleLoggingState */
  public Integer getLoggingState() {
    return loggingState;
  }

  /**
   * Sets ruleLoggingState of associated TreeItem.
   *
   * @param loggingState An Integer representing the ruleLoggingState
   */
  public void setLoggingState(Integer loggingState) {
    this.loggingState = loggingState;
  }

  /** @return True, if associated TreeItem represents an import, else false */
  public Boolean getIsImport() {
    return isImport;
  }

  /**
   * Defines the associated TreeItem as an Import or not
   *
   * @param loggingState True, if associated TreeItem is an Import, else false
   */
  public void setIsImport(Boolean isImport) {
    this.isImport = isImport;
  }


  /*****************************************************************************
   * YAML
   ****************************************************************************/

  /** Nullary constructor needed for YAML (JavaBeans convention). */
  public Properties() {}

}

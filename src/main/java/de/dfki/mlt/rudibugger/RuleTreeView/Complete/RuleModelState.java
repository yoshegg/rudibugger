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

package de.dfki.mlt.rudibugger.RuleTreeView.Complete;

import java.nio.file.Path;

/**
 * Represents a complete RuleModel TODO: explain better
 *  - the ruleLoggingState of every known rule.
 *  - the expansion state of every item in the ruleTreeView
 *  - TODO: knowledge about the scrollbar position
 *
 * Can be saved or loaded.
 *
 * Can take the CurrentRuleModel and overwrite its own knowledge.
 *
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleModelState {




  /** Save the current selection in a file. */
  public void saveRuleModelState() {

  }

  /** Load the a saved selection from a file. */
  public void loadRuleModelState(Path file) {
    /* Read in file and update RuleModelState */

    /* Apply to current RuleModel */
  }

}

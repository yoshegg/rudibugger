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
package de.dfki.mlt.rudibugger.SearchAndFind;

import de.dfki.mlt.rudibugger.DataModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains functionality to search through <code>.rudi</code> files
 * via plain-text search. As underlying tool it uses grep, which might be
 * problematic on non-unix systems.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SearchManager {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("vondaCompile");

  /** The <code>DataModel</code>. */
  private final DataModel _model;

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public SearchManager(DataModel model) { _model = model; }

  /** Represents the found results of a search. */
  public ObservableList<String[]> searchResults
    = FXCollections.observableArrayList();

  /** Represents the grep process. */
  private Process _p;


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /**
   * Searches through all the <code>.rudi</code> files.
   *
   * @param expression
   * @param caseInsensitive
   * @param wholeWord
   * @param regexExpression
   */
  public void search(String expression, boolean caseInsensitive,
    boolean wholeWord, boolean regexExpression) {

    /* Aborts an ongoing search: */
    if ((_p != null) && (_p.isAlive())) _p.destroy();

    /* Stores the result */
    List<String[]> result = new ArrayList<>();

    /* In case the search has been deleted. */
    if (expression.equals("")) {
      searchResults.setAll(result);
      return;
    }

    /* Needed to execute a UNIX command */
    List<String> commands = new ArrayList<String>() {{
      add("bash");
      add("-c");
    }};

    /* Build the grep command as a list */
    List<String> grepParts = new ArrayList<>();
    grepParts.add("grep -Rn");
    if (caseInsensitive) grepParts.add("-i");
    if (wholeWord) grepParts.add("-w");
    if (regexExpression) grepParts.add("-E");
    grepParts.add("-e \"" + expression + "\"");

    /* Flatten this list and add it to the main command */
    String grepCommand = "";
    grepCommand = grepParts.stream()
      .map((s) -> s + " ")
      .reduce(grepCommand, String::concat);
    commands.add(grepCommand);

    /* Create a new process */
    ProcessBuilder pb = new ProcessBuilder(commands);
    pb.directory(_model.project.getRudiFolder().toFile());

    /* Executes the search command */
    try {
      _p = pb.start();
      BufferedReader br
        = new BufferedReader(new InputStreamReader(_p.getInputStream()));
      _p.waitFor(50, TimeUnit.MILLISECONDS);
      br.lines().forEach(element -> {
        String[] split = element.split(":");
        result.add(split);
      });
    } catch (IOException | InterruptedException ex) {
      log.error(ex.getMessage());
    }

    searchResults.setAll(result);

  }

}

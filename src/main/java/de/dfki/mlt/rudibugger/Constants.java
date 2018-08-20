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

package de.dfki.mlt.rudibugger;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * For convenience this class contains constants that can be used anywhere in
 * rudibugger.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Constants {

 /******************************************************************************
  * DEFAULT VALUES OF SOME ESSENTIAL FILES AND FOLDERS
  *****************************************************************************/

  public static Path PATH_TO_RUDI_FOLDER = Paths.get("src/main/rudi/");
  public static Path PATH_TO_GENERATED_FOLDER
          = Paths.get("src/main/resources/generated");
  public static String COMPILE_FILE = "compile";
  public static String RUN_FILE = "run.sh";
  public static Path GLOBAL_CONFIG_FILE
          = Paths.get(System.getProperty("user.home"), ".config", "rudibugger",
                  "rudibuggerConfiguration.yml");
  public static Path RECENT_PROJECTS_FILE
          = Paths.get(System.getProperty("user.home"), ".config", "rudibugger",
                  "recentProjects.yml");
  public static Path GLOBAL_CONFIG_PATH
          = Paths.get(System.getProperty("user.home"), ".config", "rudibugger");
  public static Path GLOBAL_LAYOUT_CONFIG_FILE
          = Paths.get(System.getProperty("user.home"), ".config", "rudibugger",
                  "rudibuggerLayout.yml");


 /******************************************************************************
  * MARK AND SIGNALIZE THE USER'S REQUEST OF A NEW PROJECT
  *****************************************************************************/

  public static final boolean OVERWRITE_PROJECT = true;


 /******************************************************************************
  * MARK AND SIGNALIZE THE STATE OF THE RULEMODEL
  *****************************************************************************/

  public static final int RULE_MODEL_UNCHANGED = 0;
  public static final int RULE_MODEL_NEWLY_CREATED = 1;
  public static final int RULE_MODEL_CHANGED = 2;
  public static final int RULE_MODEL_REMOVED = 9;


 /******************************************************************************
  * SIGNALIZE THE OPENING OR CLOSING OF A PROJECT
  *****************************************************************************/

  public static final boolean PROJECT_OPEN = true;
  public static final boolean PROJECT_CLOSED = false;


 /******************************************************************************
  * MARK THE USAGE STATE OF A FILE IN A PROJECT
  *****************************************************************************/

  public static final int FILE_USED = 1;
  public static final int FILE_NOT_USED = 0;
  public static final int FILE_IS_MAIN = 2;
  public static final int FILE_IS_WRAPPER = 3;
  public static final int IS_FOLDER = 9;


  /*****************************************************************************
   * COMPILATION STATE OF <code>.rudi</code> FILES
   ****************************************************************************/

  public static final int COMPILATION_PERFECT = 1;
  public static final int COMPILATION_WITH_ERRORS = 2;
  public static final int COMPILATION_WITH_WARNINGS = 3;
  public static final int COMPILATION_FAILED = 4;
  public static final int COMPILATION_UNDEFINED = 5;
  public static final int COMPILATION_NO_PROJECT = 9;


  /*****************************************************************************
   * MODIFICATION STATE OF <code>.rudi</code> FILES
   ****************************************************************************/

  public static final int FILES_SYNCED = 10;
  public static final int FILES_OUT_OF_SYNC = 20;
  public static final int FILES_SYNC_UNDEFINED = 30;
  public static final int FILES_SYNC_NO_PROJECT = 90;


  /*****************************************************************************
   * CONNECTION STATE TO VONDA
   ****************************************************************************/

  public static final int ESTABLISHING_CONNECTION = 1;
  public static final int CONNECTED_TO_VONDA = 2;
  public static final int DISCONNECTED_FROM_VONDA = 0;


  /*****************************************************************************
   * CLOSE MODIFIED FILE
   ****************************************************************************/

  public static final int CANCEL_CLOSING = 0;
  public static final int CLOSE_BUT_SAVE_FIRST = 1;
  public static final int CLOSE_WITHOUT_SAVING = 2;


  /*****************************************************************************
   * URLS
   ****************************************************************************/

  public static final String HELP_URL
    = "https://rudibugger.readthedocs.io/en/latest/";
  public static final String GITHUB_URL
    = "https://github.com/yoshegg/rudibugger";

}

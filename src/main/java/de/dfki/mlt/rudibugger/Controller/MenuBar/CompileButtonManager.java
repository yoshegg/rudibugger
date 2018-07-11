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
package de.dfki.mlt.rudibugger.Controller.MenuBar;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.mlt.rudibugger.DataModelAdditions.ProjectManager;
import de.dfki.mlt.rudibugger.DataModelAdditions.VondaCompilation;
import javafx.scene.control.*;
import javafx.event.*;

/**
 * This class manages the look and feel of the compile button.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class CompileButtonManager {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("compileButtonMan");

  /** The <code>DataModel</code>. */
  //private final DataModel _model;


  /*****************************************************************************
   * FIELDS
   ****************************************************************************/

  /** Represents the toolbar containing the compile button(s). */
  private ToolBar toolBar;

  /** Represents the standard compile button. */
  private Button standardCompileButton;

  /** Represents the custom compile button. */
  private final SplitMenuButton extendedCompileButton = new SplitMenuButton();


  /*****************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   ****************************************************************************/

  /** Initializes an instance of this class. */
  private CompileButtonManager() {
  }

  /**
   * Initializes a new <code>CompileButtonManager</code> and returns it.
   *
   * @param model
   *        The current <code>DataModel</code>
   * @param standard
   *        The compile button
   * @param toolBar
   *        The ToolBar containing the compile button
   * @Return An instance of this class.
   */
  public static CompileButtonManager init(Button standard, ToolBar toolBar) {
    CompileButtonManager cbm = new CompileButtonManager();
    cbm.standardCompileButton = standard;
    cbm.toolBar = toolBar;
    return cbm;
  };


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  private void adaptMenu(String k, EventHandler<ActionEvent> eh) {
    extendedCompileButton.getItems().forEach(e -> e.setVisible(!e.getText().equals(k)));
    extendedCompileButton.setText(k);
    extendedCompileButton.setOnAction(eh);
  }

  /**
   * Defines the look and file of the compile button.
   *
   * Several conditions need to be checked: <br>
   *   - No compile file and no custom commands, <br>
   *   - a compile file and no custom commands, <br>
   *   - no compile file, but multiple custom commands, <br>
   *   - a compile file and at least one custom command, or <br>
   *   - no compile file, but one custom command.
   */
  public void defineCompileButton(final ProjectManager project,
      final VondaCompilation compiler) {
    if (toolBar.getItems().contains(standardCompileButton)) {
      toolBar.getItems().remove(standardCompileButton);
      toolBar.getItems().add(0, extendedCompileButton);
    }
    extendedCompileButton.getItems().clear();

    LinkedList<CustomMenuItem> commandItems = new LinkedList<>();
    Collection<String> compileCommands = project.getCompileCommandLabels();
    String defaultCmd = project.getDefaultCompileCommand();

    /* Iterate over alternative compile commands */
    for (String k : compileCommands) {
      Label l = new Label(k);
      CustomMenuItem cmi = new CustomMenuItem(l);
      cmi.setText(k);
      Tooltip t = new Tooltip(project.getCompileCommand(k));
      Tooltip.install(l, t);
      cmi.setOnAction(f -> {
        project.setDefaultCompileCommand(k);
        adaptMenu(k, e -> compiler.startCompile(project.getCompileCommand(k)));
        compiler.startCompile(project.getCompileCommand(k));
      });
      if (k.equals(defaultCmd))
        commandItems.addFirst(cmi);
      else
        commandItems.add(cmi);
    }
    commandItems.forEach(e -> extendedCompileButton.getItems().add(e));
    String k = commandItems.getFirst().getText();
    adaptMenu(k, e -> compiler.startCompile(project.getCompileCommand(k)));
  }

}

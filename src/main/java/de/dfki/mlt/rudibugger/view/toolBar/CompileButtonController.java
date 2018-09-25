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
package de.dfki.mlt.rudibugger.view.toolBar;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.mlt.rudibugger.project.Project;
import de.dfki.mlt.rudibugger.project.VondaCompiler;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.event.*;

/**
 * This class manages the look and feel of the compile button.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class CompileButtonController {

  static Logger log = LoggerFactory.getLogger("compileButtonMan");


  /* ***************************************************************************
   * FIELDS
   * **************************************************************************/

  /** Represents the ToolBar containing the compile button(s). */
  private final ToolBar _toolBar;

  /** Represents the standard compile button. */
  private final Button _standardCompileButton;

  /** Represents the custom compile button. */
  private final SplitMenuButton _extendedCompileButton = new SplitMenuButton();


  /* ***************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   * **************************************************************************/

  /** Initializes an instance of this class. */
  private CompileButtonController(ToolBar toolBar,
    Button standardCompileButton) {
    _toolBar = toolBar;
    _standardCompileButton = standardCompileButton;
  }

  /**
   * Initializes a new <code>CompileButtonController</code> and returns it.
   *
   * @param model
   *        The current <code>DataModel</code>
   * @param standardButton
   *        The compile button
   * @param toolBar
   *        The ToolBar containing the compile button
   * @Return An instance of this class.
   */
  public static CompileButtonController init(Button standardButton,
    ToolBar toolBar) {
    return new CompileButtonController(toolBar, standardButton);
  };


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  private void adaptMenu(String k, EventHandler<ActionEvent> eh) {
//    TODO: setVisible is cause for graphic errors
//    extendedCompileButton.getItems().forEach(e -> e.setVisible(!e.getText().equals(k)));
    _extendedCompileButton.setText(k);
    _extendedCompileButton.setOnAction(eh);
  }

  public void disableCompileButton() {
    if (_toolBar.getItems().contains(_extendedCompileButton)) {
      _toolBar.getItems().remove(_extendedCompileButton);
      _toolBar.getItems().add(0, _standardCompileButton);
    }
    _standardCompileButton.setDisable(true);
  }

  /** Defines the look and feel of the compile button. */
  public void linkToCompiler(final VondaCompiler compiler) {
    defineCompilerButton(compiler);
    compiler.getCompileLabelList().addListener((ListChangeListener) cl -> {
      defineCompilerButton(compiler);
    });
  }

  private void defineCompilerButton(VondaCompiler compiler) {
    _toolBar.getItems().remove(0);
    if (compiler.getCompileLabelList().size() == 1) {
      _toolBar.getItems().add(0, _standardCompileButton);
      _standardCompileButton.setText(compiler.getDefaultCompileCommandLabel());
      _standardCompileButton.setOnAction(e ->
        compiler.startCompile(compiler.getDefaultCompileCommand())
      );
    }

    else { // More than one compile command
      _toolBar.getItems().add(0, _extendedCompileButton);
      _extendedCompileButton.getItems().clear();

      _extendedCompileButton.setText(compiler.getDefaultCompileCommandLabel());
      _extendedCompileButton.setOnAction(e ->
        compiler.startCompile(compiler.getDefaultCompileCommand())
      );

      List<String> otherCompileCommandLabels = compiler.getCompileLabelList()
        .subList(1, compiler.getCompileLabelList().size());

      LinkedList<CustomMenuItem> commandItems = new LinkedList<>();
      otherCompileCommandLabels.forEach(l -> {
        CustomMenuItem cmi = new CustomMenuItem(new Label(l));
        String compileCommand = compiler.getCompileCommand(l);
        Tooltip t = new Tooltip(compileCommand);
        Tooltip.install(cmi.getContent(), t);
        cmi.setOnAction(f -> {
          compiler.setDefaultCompileCommand(l);
          compiler.startCompile(compileCommand);
        });
        commandItems.add(cmi);
      });
      _extendedCompileButton.getItems().addAll(commandItems);
    }
  }

}

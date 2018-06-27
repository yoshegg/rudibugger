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

import de.dfki.mlt.rudibugger.DataModel;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the look and feel of the compile button.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class CompileButtonManager {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("compileButtonMan");

  /** The <code>DataModel</code>. */
  private final DataModel _model;


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
  private CompileButtonManager(DataModel model) {
    _model = model;
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
  public static CompileButtonManager init(DataModel model, Button standard,
          ToolBar toolBar) {
    CompileButtonManager cbm = new CompileButtonManager(model);
    cbm.standardCompileButton = standard;
    cbm.toolBar = toolBar;
    return cbm;
  };


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

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
  public void defineCompileButton() {

    /* No compile file and no custom commands. */
    if (  (_model.project.getCompileFile() == null)
        & (_model.project.getCustomCompileCommands().isEmpty()) ) {
      resetAndDisableCompileButton();
    }

    /* A compile file and no custom commands. */
    else if ( (_model.project.getCompileFile() != null)
            & (_model.project.getCustomCompileCommands().isEmpty()) ) {
      enableCompileFileBasedCompileButton();
    }

    /* No compile file, but multiple custom commands. */
    else if ( (_model.project.getCompileFile() == null)
            & (_model.project.getCustomCompileCommands().size() > 1)) {
      enableMultipleCustomCommandsWithoutCompileFile();
    }

    /* A compile file and at least one custom command. */
    else if ( (_model.project.getCompileFile() != null)
            & (! _model.project.getCustomCompileCommands().isEmpty()) ) {
      enableMultipleCustomCommandsWithCompileFile();
    }

    /* No compile file, but one custom command. */
    else if ( (_model.project.getCompileFile() == null)
            & (_model.project.getCustomCompileCommands().size() == 1) ) {
      enableSingleCustomCommandBasedCompilation();
    }
  }

  /** Disables and resets the compile button. */
  private void resetAndDisableCompileButton() {
    if (toolBar.getItems().contains(extendedCompileButton)) {
        toolBar.getItems().remove(extendedCompileButton);
        toolBar.getItems().add(0, standardCompileButton);
      }
    standardCompileButton.setDisable(true);
    standardCompileButton.setText("Compile");
  }

  /** Enables a compile file based compile button. */
  private void enableCompileFileBasedCompileButton() {
    if (toolBar.getItems().contains(extendedCompileButton)) {
        toolBar.getItems().remove(extendedCompileButton);
        toolBar.getItems().add(0, standardCompileButton);
      }
    standardCompileButton.setDisable(false);
    standardCompileButton.setText("Compile");
    standardCompileButton.setOnAction(e ->
      _model.compiler.startCompileFileBasedCompilation());
  }

  /** Enables multiple custom commands without compile file. */
  private void enableMultipleCustomCommandsWithoutCompileFile() {
    if (toolBar.getItems().contains(standardCompileButton)) {
        toolBar.getItems().remove(standardCompileButton);
        toolBar.getItems().add(0, extendedCompileButton);
    }
    extendedCompileButton.getItems().clear();

    Map<String, CustomMenuItem> customCommandItems = new HashMap<>();

    /* Iterate over alternative compile commands */
    for (String k : _model.project.getCustomCompileCommands().keySet()) {
      Label l = new Label(k);
      CustomMenuItem cmi = new CustomMenuItem(l);
      String cmd = _model.project.getCustomCompileCommands().get(k);
      Tooltip t = new Tooltip(cmd);
      Tooltip.install(l, t);
      cmi.setOnAction(f -> {
        _model.compiler.startCompile(cmd);
        _model.project.defaultCompileCommandProperty().set(k);
      });
      customCommandItems.put(k, cmi);
    }

    if (customCommandItems.keySet()
            .contains(_model.project.getDefaultCompileCommand())) {
      customCommandItems.remove(_model.project.getDefaultCompileCommand());
      String commandTitle = _model.project.getDefaultCompileCommand();
      String command = _model.project.getCustomCompileCommands()
              .get(commandTitle);
      extendedCompileButton.setText(commandTitle);
      extendedCompileButton.setOnAction(e ->
        _model.compiler.startCompile(command));
    } else {
      String firstCustomCommand = ((String[]) _model.project
              .getCustomCompileCommands().keySet().toArray())[0];
      customCommandItems.remove(firstCustomCommand);
      String command = _model.project.getCustomCompileCommands()
            .get(firstCustomCommand);
      extendedCompileButton.setText(firstCustomCommand);
      extendedCompileButton.setOnAction(e -> {
        _model.compiler.startCompile(command);
        _model.project.defaultCompileCommandProperty().set(firstCustomCommand);
      });
    }

    customCommandItems.keySet().forEach(e ->
      extendedCompileButton.getItems().add(customCommandItems.get(e)));
  }

  /** Enables multiple custom commands with compile file. */
  private void enableMultipleCustomCommandsWithCompileFile() {
    if (toolBar.getItems().contains(standardCompileButton)) {
      toolBar.getItems().remove(standardCompileButton);
      toolBar.getItems().add(0, extendedCompileButton);
    }
    extendedCompileButton.getItems().clear();

    Map<String, CustomMenuItem> customCommandItems = new HashMap<>();

    /* Iterate over alternative compile commands */
    for (String k : _model.project.getCustomCompileCommands().keySet()) {
      Label l = new Label(k);
      CustomMenuItem cmi = new CustomMenuItem(l);
      String cmd = _model.project.getCustomCompileCommands().get(k);
      Tooltip t = new Tooltip(cmd);
      Tooltip.install(l, t);
      cmi.setOnAction(f -> {
        _model.compiler.startCompile(cmd);
        _model.project.defaultCompileCommandProperty().set(k);
      });
      customCommandItems.put(k, cmi);
    }

    if (customCommandItems.keySet()
            .contains(_model.project.getDefaultCompileCommand())) {
      customCommandItems.remove(_model.project.getDefaultCompileCommand());
      String commandTitle = _model.project.getDefaultCompileCommand();
      String command = _model.project.getCustomCompileCommands()
              .get(commandTitle);
      extendedCompileButton.setText(commandTitle);
      extendedCompileButton.setOnAction(e ->
        _model.compiler.startCompile(command));

      /** Add standard compile file to list. */
      Label l = new Label("Compile");
      CustomMenuItem cmi = new CustomMenuItem(l);
      Tooltip t = new Tooltip("Run compile file");
      Tooltip.install(l, t);
      cmi.setOnAction(f -> {
        _model.compiler.startCompileFileBasedCompilation();
        _model.project.defaultCompileCommandProperty().set("");
      });
      customCommandItems.put(l.getText(), cmi);

    } else {
      extendedCompileButton.setText("Compile");
      extendedCompileButton.setOnAction(e ->
        _model.compiler.startCompileFileBasedCompilation());
    }

    customCommandItems.keySet().forEach(e ->
      extendedCompileButton.getItems().add(customCommandItems.get(e)));
  }

  /** Enables a single custom compile command. */
  private void enableSingleCustomCommandBasedCompilation() {
    if (toolBar.getItems().contains(extendedCompileButton)) {
        toolBar.getItems().remove(extendedCompileButton);
        toolBar.getItems().add(0, standardCompileButton);
      }
    String commandTitle = ((String[]) _model.project.getCustomCompileCommands()
            .keySet().toArray())[0];
    String command = _model.project.getCustomCompileCommands()
            .get(commandTitle);
    standardCompileButton.setDisable(false);
    standardCompileButton.setText(commandTitle);
    standardCompileButton.setOnAction(e ->
      _model.compiler.startCompile(command));
  }

}

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

package de.dfki.mlt.rudibugger.StatusBar;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.RuleModel.ImportInfoExtended;
import static de.dfki.mlt.rudimant.common.Constants.RULE_FILE_EXTENSION;
import de.dfki.mlt.rudimant.common.ErrorWarningInfo;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;

/**
 * This class controls the behaviour of the compile indicator in the lower left
 * of rudibugger. It indicates the outcome of the last compilation attempt.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class CompileIndicator {

  /** The Controller. */
  private StatusBarController _controller;

  /** An icon showing the current sync state. */
  private ImageView _indicator;

  /** Shows an explanation of the current sync state. */
  private Tooltip _tooltip;

  /** Path for icons describing compilation status. */
  private static final String ICONS_PATH
          = "file:src/main/resources/icons/compileStatus/";

  /** Map of compilation status icons. */
  private static final HashMap<Integer, Image> ICONS
    = new HashMap<Integer, Image>() {{
      put(COMPILATION_PERFECT,       new Image(ICONS_PATH + "okay.png"));
      put(COMPILATION_WITH_ERRORS,   new Image(ICONS_PATH + "errors.png"));
      put(COMPILATION_WITH_WARNINGS, new Image(ICONS_PATH + "warnings.png"));
      put(COMPILATION_FAILED,        new Image(ICONS_PATH + "failed.png"));
      put(COMPILATION_UNDEFINED,     new Image(ICONS_PATH + "undefined.png"));
   }};

  /** Map of compilation status tooltip's texts. */
  private static final HashMap<Integer, String> MESSAGES
    = new HashMap<Integer, String>() {{
      put(COMPILATION_PERFECT,       "Compilation succeeded without problems");
      put(COMPILATION_WITH_ERRORS,   "Compilation succeeded with errors.");
      put(COMPILATION_WITH_WARNINGS, "Compilation succeeded with warnings.");
      put(COMPILATION_FAILED,        "Compilation failed.");
      put(COMPILATION_UNDEFINED,     "Compilation state unknown.");
    }};

  /** Creates a new instance of <code>CompileIndicator</code>, creates a tooltip
   * instance and links everything to the <code>StatusBarController</code>.
   *
   * @param indicator
   * @param controller
   */
  public CompileIndicator(ImageView indicator, StatusBarController controller) {
    _indicator = indicator;
    _tooltip = new Tooltip();
    _controller = controller;

    /* Initializes the default look and behaviour if no project is loaded. */
    _indicator.setImage(ICONS.get(COMPILATION_UNDEFINED));
    _tooltip.setText(MESSAGES.get(COMPILATION_UNDEFINED));
    Tooltip.install(_indicator, _tooltip);
  }

  /** Responsible for updating tooltip and icon. */
  private final ChangeListener<Number> listener = ((cl, ov, nv) -> {
      int val = nv.intValue();
      String msg = MESSAGES.get(val);

      if (val != COMPILATION_UNDEFINED)
        _controller.setStatusBar(msg);

      _tooltip.setText(msg);
      Tooltip.install(_indicator, _tooltip);

      _indicator.setImage(ICONS.get(val));
  });

  /**
   * Links a given property to the listener of <code>CompileIndicator</code>.
   *
   * @param property
   */
  public void linkListenerToProperty(IntegerProperty property) {
    property.addListener(listener);
  }

  /**
   * This <code>EventHandler</code> contains a <code>ContextMenu</code> to be
   * opened when requested.
   */
  private final EventHandler<? super ContextMenuEvent> contextMenu = (value -> {
    ContextMenu cm = new ContextMenu();

    DataModel model = _controller.getModel();
    LinkedHashMap<ErrorWarningInfo, ImportInfoExtended> errorInfos;
    errorInfos = model.ruleModel.getErrorInfos();
    LinkedHashMap<ErrorWarningInfo, ImportInfoExtended> warnInfos;
    warnInfos = model.ruleModel.getWarnInfos();

    addErrorWarningInfosToContextMenu(cm, "error", errorInfos, model);
    if ((! errorInfos.isEmpty()) && (! warnInfos.isEmpty()))
      cm.getItems().add(new SeparatorMenuItem());
    addErrorWarningInfosToContextMenu(cm, "warning", warnInfos, model);

    cm.show(_indicator, value.getScreenX(), value.getScreenY());
  });

  /**
   * Adds warnings or errors from the current <code>RuleModel</code> to the
   * <code>ContextMenu</code> of the <code>CompileIndicator</code>.
   *
   * @param cm
   * @param type
   * @param data
   * @param model
   */
  private void addErrorWarningInfosToContextMenu(ContextMenu cm, String type,
    LinkedHashMap<ErrorWarningInfo, ImportInfoExtended> data, DataModel model) {
    for (ErrorWarningInfo e : data.keySet()) {
      ImportInfoExtended item = data.get(e);
      String shortType = (("warning".equals(type)) ? "WARN" : "ERROR");
      String msg = shortType + ":"
                 + item.getLabel() + RULE_FILE_EXTENSION + ":"
                 + e.getLocation().getLineNumber() + ": "
                 + e.getMessage();
      Label label = new Label(msg);
      CustomMenuItem errorItem = new CustomMenuItem(label);
      errorItem.setOnAction(f -> {
        model.rudiLoad.openRule(item.getAbsolutePath(),
          e.getLocation().getLineNumber());
      });
      cm.getItems().add(errorItem);
    }
  }

  /**
   * Defines the <code>ContextMenu</code> of this compileIndicator.
   */
  public void defineContextMenu() {
    _indicator.setOnContextMenuRequested(contextMenu);
  }

}

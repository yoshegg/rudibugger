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

package de.dfki.mlt.rudibugger.view.ruleTreeView;

import de.dfki.mlt.rudibugger.DataModelAdditions.GlobalConfiguration;
import de.dfki.mlt.rudibugger.project.Project;
import static de.dfki.mlt.rudimant.common.ErrorInfo.ErrorType.*;

import de.dfki.mlt.rudibugger.project.ruleModel.RuleInfoExtended;
import de.dfki.mlt.rudibugger.project.ruleModel.ImportInfoExtended;
import de.dfki.mlt.rudimant.common.BasicInfo;
import static de.dfki.mlt.rudimant.common.Constants.*;
import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * Used to define the view of cells of ruleTreeView.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class BasicInfoTreeCell extends TreeCell<BasicInfo> {

  /*****************************************************************************
   * CONSTANTS
   ****************************************************************************/

  /** Icon path of imports. */
  static final String ICON_PATH_IMPORTS
          = "file:src/main/resources/icons/RudiLogFileStatus/";

  /** Map of import icons. */
  static final HashMap<Integer, Image> ICONS_IMPORTS
          = new HashMap<Integer, Image>() {{
    put(STATE_ALWAYS,     new Image(ICON_PATH_IMPORTS + "Always.png"));
    put(STATE_IF_TRUE,    new Image(ICON_PATH_IMPORTS + "IfTrue.png"));
    put(STATE_IF_FALSE,   new Image(ICON_PATH_IMPORTS + "IfFalse.png"));
    put(STATE_NEVER,      new Image(ICON_PATH_IMPORTS + "Never.png"));
    put(STATE_PARTLY,     new Image(ICON_PATH_IMPORTS + "Partly.png"));
    put(STATE_RULELESS,   new Image(ICON_PATH_IMPORTS + "NoRule.png"));
  }};

  /** Icon path of rules. */
  static final String ICON_PATH_RULES
          = "file:src/main/resources/icons/RudiLogRuleStatus/";

  /** Map of rule icons. */
  static final HashMap<Integer, Image> ICONS_RULES
          = new HashMap<Integer, Image>() {{
    put(STATE_ALWAYS,   new Image(ICON_PATH_RULES + "Always.png"));
    put(STATE_IF_TRUE,  new Image(ICON_PATH_RULES + "IfTrue.png"));
    put(STATE_IF_FALSE, new Image(ICON_PATH_RULES + "IfFalse.png"));
    put(STATE_NEVER,    new Image(ICON_PATH_RULES + "Never.png"));
    put(STATE_PARTLY,   new Image(ICON_PATH_RULES + "Partly.png"));
  }};


  /*****************************************************************************
   * FIELDS
   ****************************************************************************/

  /** Icon of the TreeItem, indication rule logging state. */
  private ImageView stateIndicator;

  /** TODO */
  private final Project _project;

  /** TODO */
  private final GlobalConfiguration _globalConf;

  /* ***************************************************************************
   * CONSTRUCTOR
   * **************************************************************************/

  /** Initializes a new cell. */
  public BasicInfoTreeCell(Project project, GlobalConfiguration globalConf) {
    super();
    _project = project;
    _globalConf = globalConf;
  }

  /*****************************************************************************
   * PSEUDOCLASSES
   ****************************************************************************/

  /** Used to visually distinguish erroneous imports with CSS. */
  private final PseudoClass errorsInImportClass
          = PseudoClass.getPseudoClass("errorsInImport");

  /** Used to visually distinguish imports with warning with CSS. */
  private final PseudoClass warningsInImportClass
          = PseudoClass.getPseudoClass("warningsInImport");


  /*****************************************************************************
   * LISTENERS
   ****************************************************************************/

  /** Used to listen to rule state changes. */
  private final ChangeListener<Number> ruleStateListener = ((o, ov, nv)
    -> this.stateIndicator.setImage(ICONS_RULES.get(nv.intValue())));

  /** Used to listen to import state changes. */
  private final ChangeListener<Number> importStateListener = ((o, ov, nv)
    -> this.stateIndicator.setImage(ICONS_IMPORTS.get(nv.intValue())));


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  @Override
  protected void updateItem(BasicInfo bi, boolean empty) {

    /* Remove old listener of the cell */
    BasicInfo oldItem = getItem();
    if (oldItem != null) {
      if (oldItem instanceof RuleInfoExtended)
        ((RuleInfoExtended) oldItem).stateProperty()
          .removeListener(ruleStateListener);
      else
        ((ImportInfoExtended) oldItem).stateProperty()
          .removeListener(importStateListener);
    }

    super.updateItem(bi, empty);

    if (empty || bi == null) {

      setText(null);
      setGraphic(null);
      pseudoClassStateChanged(errorsInImportClass, false);
      pseudoClassStateChanged(warningsInImportClass, false);

      /* define click on empty cell */
      this.setOnMouseClicked(e -> {
          e.consume();
      });

      /* define context menu request on empty cell */
      this.setOnContextMenuRequested(e -> {
          e.consume();
      });

    } else {

      /* RULE */
      if (bi instanceof RuleInfoExtended) {
        RuleInfoExtended ri = (RuleInfoExtended) bi;
        stateIndicator = new ImageView(ICONS_RULES.get(ri.getState()));
        pseudoClassStateChanged(errorsInImportClass, false);
        pseudoClassStateChanged(warningsInImportClass, false);

        /* define a listener to reflect the rule logging state */
        ri.stateProperty().addListener(ruleStateListener);

        /* define the shown content of the cell */
        HBox hbox = new HBox();
        hbox.getChildren().addAll(stateIndicator, new Text(bi.getLabel()));
        hbox.setSpacing(5.0);
        hbox.setAlignment(Pos.CENTER_LEFT);
        setText(null);
        setGraphic(hbox);

        /* define the context menu */
        this.setOnContextMenuRequested(e -> {
          RuleContextMenu contextMenu
                  = new RuleContextMenu(ri, _project, _globalConf);
          contextMenu.show(this, e.getScreenX(), e.getScreenY());
        });

        /* define a click on the graphic / checkbox */
        stateIndicator.setOnMouseClicked(e -> {
          if (e.getButton() == MouseButton.PRIMARY)
            ri.cycleThroughStates();
          if (e.getClickCount() == 2 ) e.consume();
        });

        /* define double click on cell: open rule (file at specific line) */
        this.setOnMouseClicked(e -> {
          if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
            _project.openRule(ri.getSourceFile(), ri.getLine());
          }
        });
      }

      /* IMPORT */
      else {
        ImportInfoExtended ii = (ImportInfoExtended) bi;
        if (ii.containsRules())
          stateIndicator = new ImageView(ICONS_IMPORTS.get(ii.getState()));
        else
          stateIndicator = new ImageView(ICONS_IMPORTS.get(STATE_RULELESS));

        /* visually indicate errors and warnings happened during compile */
        if (ii.getErrors().stream().anyMatch(ewi ->
                ewi.getType() == ERROR || ewi.getType() == PARSE_ERROR)) {
          pseudoClassStateChanged(errorsInImportClass, true);
          pseudoClassStateChanged(warningsInImportClass, false);
        } else if (ii.getErrors().stream().anyMatch(ewi ->
                ewi.getType() == WARNING)) {
          pseudoClassStateChanged(warningsInImportClass, true);
          pseudoClassStateChanged(errorsInImportClass, false);
        } else {
          pseudoClassStateChanged(errorsInImportClass, false);
          pseudoClassStateChanged(warningsInImportClass, false);

        }

        /* define a listener to reflect the rule logging state */
        ii.stateProperty().addListener(importStateListener);

        /* define the shown content of the cell */
        setText(bi.getLabel());
        setGraphic(stateIndicator);

        /* define the context menu */
        this.setOnContextMenuRequested(e -> {
          ImportContextMenu contextMenu
                  = new ImportContextMenu(ii, _project, _globalConf);
          contextMenu.show(this, e.getScreenX(), e.getScreenY());
        });

        /* define double click on cell */
        this.setOnMouseClicked(e -> {
          if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
            _project.openFile(ii.getAbsolutePath());
          }
        });
      }

      /* disable doubleclick expand/collapse when clicking on the cell */
      this.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
        if (e.getClickCount() % 2 == 0
                && e.getButton().equals(MouseButton.PRIMARY)) {
          e.consume();
        }
      });
    }
  }
}

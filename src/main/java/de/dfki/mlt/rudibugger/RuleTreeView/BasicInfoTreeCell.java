/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import de.dfki.mlt.rudimant.common.BasicInfo;
import static de.dfki.mlt.rudimant.common.Constants.*;
import java.util.HashMap;
import javafx.css.PseudoClass;
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

  /** Icon path of imports. */
  static final String ICON_PATH_IMPORTS
          = "file:src/main/resources/icons/RudiLogFileStatus/";

  /** Map of import icons. */
  static final HashMap<Integer, Image> ICONS_IMPORTS = new HashMap<Integer, Image>() {{
    put(STATE_ALWAYS,   new Image(ICON_PATH_IMPORTS + "Always.png"));
    put(STATE_IF_TRUE,  new Image(ICON_PATH_IMPORTS + "IfTrue.png"));
    put(STATE_IF_FALSE, new Image(ICON_PATH_IMPORTS + "IfFalse.png"));
    put(STATE_NEVER,    new Image(ICON_PATH_IMPORTS + "Never.png"));
    put(STATE_PARTLY,   new Image(ICON_PATH_IMPORTS + "Partly.png"));
  }};

  /** Icon path of rules. */
  static final String ICON_PATH_RULES
          = "file:src/main/resources/icons/RudiLogRuleStatus/";

  /** Map of rule icons. */
  static final HashMap<Integer, Image> ICONS_RULES = new HashMap<Integer, Image>() {{
    put(STATE_ALWAYS,   new Image(ICON_PATH_RULES + "Always.png"));
    put(STATE_IF_TRUE,  new Image(ICON_PATH_RULES + "IfTrue.png"));
    put(STATE_IF_FALSE, new Image(ICON_PATH_RULES + "IfFalse.png"));
    put(STATE_NEVER,    new Image(ICON_PATH_RULES + "Never.png"));
    put(STATE_PARTLY,   new Image(ICON_PATH_RULES + "Partly.png"));
  }};

  /** Used to visually distinguish erroneous imports with CSS */
  private final PseudoClass erroneousImportClass
          = PseudoClass.getPseudoClass("erroneousImport");

  @Override
  protected void updateItem(BasicInfo bi, boolean empty) {
    super.updateItem(bi, empty);

    if (empty || bi == null) {

      setText(null);
      setGraphic(null);
      pseudoClassStateChanged(erroneousImportClass, false);

      /* define click on empty cell */
      this.setOnMouseClicked(e -> {
          e.consume();

      });
      /* define context menu request on empty cell */
      this.setOnContextMenuRequested(e -> {
          e.consume();
      });

    } else {

      ImageView stateIndicator;

      /* RULE */
      if (bi instanceof RuleInfoExtended) {
        RuleInfoExtended ri = (RuleInfoExtended) bi;
        stateIndicator = new ImageView(ICONS_RULES.get(ri.getState()));
        pseudoClassStateChanged(erroneousImportClass, false);

        /* define a listener to reflect the rule logging state */
        ri.stateProperty().addListener((o, oldVal, newVal) -> {
          if (oldVal != newVal)
            stateIndicator.setImage(ICONS_RULES.get(newVal.intValue()));
        });

        /* define the shown content of the cell */
        HBox hbox = new HBox();
        hbox.getChildren().addAll(stateIndicator, new Text(bi.getLabel()));
        hbox.setSpacing(5.0);
        setText(null);
        setGraphic(hbox);

        /* define the context menu */
        this.setOnContextMenuRequested(e -> {
          RuleContextMenu contextMenu = new RuleContextMenu(ri);
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
            ri._model.openRule(ri.getSourceFile(), ri.getLine());
          }
        });
      }

      /* IMPORT */
      else {
        ImportInfoExtended ii = (ImportInfoExtended) bi;
        stateIndicator = new ImageView(ICONS_IMPORTS.get(ii.getState()));

        /* visually indicate errors happened during compile */
        pseudoClassStateChanged(erroneousImportClass, !ii.getErrors().isEmpty());

        /* define a listener to reflect the rule logging state */
        ii.stateProperty().addListener((o, oldVal, newVal) -> {
          if (oldVal != newVal)
            stateIndicator.setImage(ICONS_IMPORTS.get(newVal.intValue()));
        });

        /* define the shown content of the cell */
        setText(bi.getLabel());
        setGraphic(stateIndicator);

        /* define the context menu */
        this.setOnContextMenuRequested(e -> {
          ImportContextMenu contextMenu = new ImportContextMenu(ii);
          contextMenu.show(this, e.getScreenX(), e.getScreenY());
        });

        /* define double click on cell */
        this.setOnMouseClicked(e -> {
          if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
            ii._model.openFile(ii.getAbsolutePath());
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

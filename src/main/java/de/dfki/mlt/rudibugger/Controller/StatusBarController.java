/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.Controller;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.RuleTreeView.ImportInfoExtended;
import de.dfki.mlt.rudimant.common.ErrorWarningInfo;
import java.util.HashMap;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.image.Image;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class StatusBarController {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("rudiLog");

  /** The model. */
  private DataModel _model;

  /**
   * Links model to controller and initializes listeners
   *
   * @param model
   */
  public void initModel(DataModel model) {
    if (this._model != null) {
      throw new IllegalStateException("Model can only be initialized once.");
    }
    _model = model;

    statusBar.textProperty().bindBidirectional(model.statusBarProperty());

    compileIndicator.setImage(
      ICONS_COMPILATION_STATUS.get(COMPILATION_UNDEFINED));
    compileIndicatorTooltip.setText("Compilation state unknown.");
      Tooltip.install(compileIndicator, compileIndicatorTooltip);

      compileIndicator.setOnContextMenuRequested(value -> {
        ContextMenu cm = new ContextMenu();

        int counter = 0;
        for (ErrorWarningInfo e : _model.ruleModel.errorInfos.keySet()) {
          ImportInfoExtended item = _model.ruleModel.errorInfos.get(e);
          String msg = "Go to error " + (++counter)
                     + " ("
                     + "line " + e.getLocation().getLineNumber() + ", "
                     + "file " + item.getLabel()
                     + ")";
          Label label = new Label(msg);
          CustomMenuItem errorItem = new CustomMenuItem(label);
          Tooltip t = new Tooltip(e.getMessage());
          Tooltip.install(label, t);
          errorItem.setOnAction(f -> {
            _model.openRule(item.getAbsolutePath(),
                            e.getLocation().getLineNumber());
          });
          cm.getItems().add(errorItem);
        }

        if (   (! _model.ruleModel.errorInfos.isEmpty())
            && (! _model.ruleModel.warnInfos.isEmpty())) {
          cm.getItems().add(new SeparatorMenuItem());
        }

        counter = 0;
        for (ErrorWarningInfo e : _model.ruleModel.warnInfos.keySet()) {
          ImportInfoExtended item = _model.ruleModel.warnInfos.get(e);
          String msg = "Go to warning " + (++counter)
                     + " ("
                     + "line " + e.getLocation().getLineNumber() + ", "
                     + "file " + item.getLabel()
                     + ")";
          Label label = new Label(msg);
          CustomMenuItem errorItem = new CustomMenuItem(label);
          Tooltip t = new Tooltip(e.getMessage());
          Tooltip.install(label, t);
          errorItem.setOnAction(f -> {
            _model.openRule(item.getAbsolutePath(),
                            e.getLocation().getLineNumber());
          });
          cm.getItems().add(errorItem);
        }

        cm.show(compileIndicator, value.getScreenX(), value.getScreenY());

      });

    syncIndicator.setImage(
      ICONS_SYNC_STATUS.get(FILES_SYNC_UNDEFINED));
    syncIndicatorTooltip.setText(".rudi and compiled .java compile status unknown."
                               + " (Probably never compiled.)");
      Tooltip.install(syncIndicator, syncIndicatorTooltip);

    /* Listen to modifications of .rudi files. */
    _model._modifiedFilesProperty().addListener((o, ov, nv) -> {
      String msg = "";

      switch (nv.intValue()) {
        case FILES_SYNCED:
          msg = ".rudi files and compiled .java files up-to-date.";
//          statusBar.textProperty().setValue(msg);
          break;
        case FILES_OUT_OF_SYNC:
          msg = ".rudi files out of sync.";
          statusBar.textProperty().setValue(msg);
          break;
        case FILES_SYNC_UNDEFINED:
          msg = ".rudi and compiled .java compile status unknown."
              + " (Probably never compiled.)";
//          statusBar.textProperty().setValue(msg);
          break;
      }
      syncIndicatorTooltip.setText(msg);
      Tooltip.install(syncIndicator, syncIndicatorTooltip);
      syncIndicator.setImage(ICONS_SYNC_STATUS.get(nv.intValue()));
    });

    /* Listen to the outcome of the last compilation try. */
    _model._compilationStateProperty().addListener((o, ov, nv) -> {
      String msg = "";

      switch (nv.intValue()) {
        case COMPILATION_PERFECT:
          msg = "Compilation succeeded without problems.";
          statusBar.textProperty().setValue(msg);
          break;
        case COMPILATION_WITH_ERRORS:
          msg = "Compilation succeeded with errors.";
          statusBar.textProperty().setValue(msg);
          break;
        case COMPILATION_WITH_WARNINGS:
          msg = "Compilation succeeded with warnings.";
          statusBar.textProperty().setValue(msg);
          break;
        case COMPILATION_FAILED:
          msg = "Compilation failed.";
          statusBar.textProperty().setValue(msg);
          break;
        case COMPILATION_UNDEFINED:
          msg = "Compilation state unknown.";
          break;
      }
      compileIndicatorTooltip.setText(msg);
      Tooltip.install(compileIndicator, compileIndicatorTooltip);
      compileIndicator.setImage(ICONS_COMPILATION_STATUS.get(nv.intValue()));
    });
  }

  /** Path for icons describing compilation status. */
  private static final String ICON_COMPILATION_STATUS_PATH
          = "file:src/main/resources/icons/compileStatus/";

  /** Map of compilation status icons. */
  private static final HashMap<Integer, Image> ICONS_COMPILATION_STATUS
    = new HashMap<Integer, Image>() {{
      put(COMPILATION_PERFECT,
        new Image(ICON_COMPILATION_STATUS_PATH + "okay.png"));
      put(COMPILATION_WITH_ERRORS,
        new Image(ICON_COMPILATION_STATUS_PATH + "errors.png"));
      put(COMPILATION_WITH_WARNINGS,
        new Image(ICON_COMPILATION_STATUS_PATH + "warnings.png"));
      put(COMPILATION_FAILED,
        new Image(ICON_COMPILATION_STATUS_PATH + "failed.png"));
      put(COMPILATION_UNDEFINED,
        new Image(ICON_COMPILATION_STATUS_PATH + "undefined.png"));
   }};

  /** Path for icons describing sync status. */
  private static final String ICON_SYNC_STATUS_PATH
          = "file:src/main/resources/icons/syncStatus/";

  /** Map of sync status icons. */
  private static final HashMap<Integer, Image> ICONS_SYNC_STATUS
    = new HashMap<Integer, Image>() {{
      put(FILES_SYNCED,
        new Image(ICON_SYNC_STATUS_PATH + "okay.png"));
      put(FILES_OUT_OF_SYNC,
        new Image(ICON_SYNC_STATUS_PATH + "out.png"));
      put(FILES_SYNC_UNDEFINED,
        new Image(ICON_SYNC_STATUS_PATH + "undefined.png"));
   }};

  /** StatusBar's label at the bottom. */
  @FXML
  private Label statusBar;

  /** Icon for sync status of .rudi and .java code. */
  @FXML
  private ImageView syncIndicator;

  /** Tooltip for syncIndicator. */
  private final Tooltip syncIndicatorTooltip = new Tooltip();

  /** Icon for outcome of last compilation. */
  @FXML
  private ImageView compileIndicator;

  /** Tooltip for compileIndicator. */
  private final Tooltip compileIndicatorTooltip = new Tooltip();

}

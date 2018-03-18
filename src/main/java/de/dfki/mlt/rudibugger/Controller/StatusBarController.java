/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.Controller;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.RuleTreeView.ImportInfoExtended;
import de.dfki.mlt.rudibugger.StatusBar.CompileIndicator;
import de.dfki.mlt.rudibugger.StatusBar.SyncIndicator;
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
import javafx.scene.input.ContextMenuEvent;

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

    /* Initialize the sync state indicator in the lower left. */
    SyncIndicator syncIndicator = new SyncIndicator(_syncIndicator, this);
    syncIndicator.linkListenerToProperty(_model._modifiedFilesProperty());

    /* Initialize the compilation state indicator in the lower left. */
    CompileIndicator compileIndicator
      = new CompileIndicator(_compileIndicator, this);
    compileIndicator.linkListenerToProperty(_model._compilationStateProperty());
    compileIndicator.setContextMenu();

  }

  public void setStatusBar(String text) {
    statusBar.setText(text);
  }

  public DataModel getModel() { return _model; }

  /** StatusBar's label at the bottom. */
  @FXML
  private Label statusBar;

  /** Icon for sync status of .rudi and .java code. */
  @FXML
  private ImageView _syncIndicator;

  /** Icon for outcome of last compilation. */
  @FXML
  private ImageView _compileIndicator;

}

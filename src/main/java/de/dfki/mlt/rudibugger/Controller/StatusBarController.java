/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.Controller;

import de.dfki.mlt.rudibugger.DataModel;
import java.util.HashMap;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.image.Image;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class StatusBarController {

  /* the logger */
  static Logger log = LoggerFactory.getLogger("rudiLog");

  /* the model */
  private DataModel model;

  public void initModel(DataModel model) {
    if (this.model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    this.model = model;

    statusBar.textProperty().bindBidirectional(model.statusBarProperty());

    syncIndicator.setImage(ICONS_SYNC_STATUS.get(true));

    model._modifiedFilesProperty().addListener((o, ov, nv) -> {
      if (nv) {
        statusBar.textProperty().setValue(".rudi files out of sync.");
        syncIndicator.setImage(ICONS_SYNC_STATUS.get(false));
      } else {
        statusBar.textProperty().setValue(".rudi files compiled.");
        syncIndicator.setImage(ICONS_SYNC_STATUS.get(true));
      }
    });

  }

    /** Icon path of imports. */
  static final String ICON_SYNC_STATUS
          = "file:src/main/resources/icons/syncStatus/";

  /** Map of import icons. */
  static final HashMap<Boolean, Image> ICONS_SYNC_STATUS
          = new HashMap<Boolean, Image>() {{
    put(true,   new Image(ICON_SYNC_STATUS + "okay.png"));
    put(false,  new Image(ICON_SYNC_STATUS + "out.png"));
  }};

  /* statusbar at the bottom */
  @FXML
  private Label statusBar;

  @FXML
  private ImageView syncIndicator;
}

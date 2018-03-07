/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RuleTreeView;

import static de.dfki.mlt.rudimant.common.Constants.*;
import java.util.HashMap;
import javafx.geometry.Pos;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ImportTreeCell extends TreeCell<ImportInfoExtended> {

  /** Icon path */
  static final String ICON_PATH
          = "file:src/main/resources/icons/RudiLogFileStatus/";

  /** Map of icons */
  static HashMap<Integer, Image> ICONS = new HashMap<Integer, Image>() {{
    put(STATE_ALWAYS,   new Image(ICON_PATH + "Always.png"));
    put(STATE_IF_TRUE,  new Image(ICON_PATH + "IfTrue.png"));
    put(STATE_IF_FALSE, new Image(ICON_PATH + "IfFalse.png"));
    put(STATE_NEVER,    new Image(ICON_PATH + "Never.png"));
    put(STATE_PARTLY,   new Image(ICON_PATH + "Partly.png"));
  }};

  @Override
  protected void updateItem(ImportInfoExtended ii, boolean empty) {
    super.updateItem(ii, empty);

    if (empty || ii == null) {

      setText(null);
      setGraphic(null);

    } else {

      _stateIndicator = new ImageView(ICONS.get(ii.getState()));
      _label = new Text(ii.getLabel());

      if (! ii.getErrors().isEmpty()) { _label.setFill(Color.RED); }

      _view = new HBox();
      _view.getChildren().addAll(
        _stateIndicator,
        new TextFlow(_label)
      );
      _view.setAlignment(Pos.CENTER_LEFT);

      setText(null);
      setGraphic(_view);

      defineControls(ii);

    }
  }

  private void defineControls(ImportInfoExtended ii) {
    /* the specific context menu for imports */
    _view.setOnContextMenuRequested((e) -> {
      ImportContextMenu contextMenu = new ImportContextMenu(ii);
      contextMenu.show(_view, e.getScreenX(), e.getScreenY());
    });

//    /* open File if clicked on label */
//    _view.getChildren().get(1).setOnMouseClicked((MouseEvent e) -> {
//      if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
//        _model.openFile(ii.getFilePath());
//      }
//    });
  }

  private ImageView _stateIndicator;
  private Text _label;
  private HBox _view;

}

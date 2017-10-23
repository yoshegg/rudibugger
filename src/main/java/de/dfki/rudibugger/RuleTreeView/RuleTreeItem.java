/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RuleTreeView;

import static de.dfki.rudibugger.Constants.*;
import de.dfki.rudibugger.RuleStore.Rule;
import de.dfki.rudibugger.DataModel;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * This class represents every known fact about a rule:
 * name, source file, line and logging status
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleTreeItem extends BasicTreeItem {

  /* the associated file */
  private final Path _file;

  /* the different icons used as indicator */
  static final String RULE_ICON_PATH
          = "file:src/main/resources/icons/RudiLogRuleStatus/";
  static Image imgAlways = new Image(RULE_ICON_PATH + "Always.png");
  static Image imgIfTrue = new Image(RULE_ICON_PATH + "IfTrue.png");
  static Image imgIfFalse = new Image(RULE_ICON_PATH + "IfFalse.png");
  static Image imgNever = new Image(RULE_ICON_PATH + "Never.png");
  static Image imgPartly = new Image(RULE_ICON_PATH + "Partly.png");

  /* model */
  DataModel _model;

  /* the constructor */
  public RuleTreeItem(Rule content, DataModel model) {
    super(content.getRuleName(), content.getLine());
    _model = model;
    _file = Paths.get(model.getRootFolder() + "/"
            + PATH_TO_RUDI_FILES + content.getSource() + ".rudi");

    /* the specific context menu for rules */
    _hb.setOnContextMenuRequested((ContextMenuEvent e) -> {
      RuleContextMenu contextMenu = new RuleContextMenu(this);
      contextMenu.show(_hb, e.getScreenX(), e.getScreenY());
    });

    /* switch through the different states when clicking on the CheckBox */
    _hb.getChildren().get(0).setOnMouseClicked((MouseEvent e) -> {
      if (e.getClickCount() == 1 && e.getButton() == MouseButton.PRIMARY) {
        cycleThroughStates();
      }
    });
  }

  /* returns the requested checkbox icon */
  public static Image getImage(int state) {
    switch (state) {
      case STATE_ALWAYS:
        return imgAlways;
      case STATE_IF_TRUE:
        return imgIfTrue;
      case STATE_IF_FALSE:
        return imgIfFalse;
      case STATE_NEVER:
        return imgNever;
      case STATE_PARTLY:
        return imgPartly;
      default:
        return null;
    }
  }

  /* cycle through different states */
  private void cycleThroughStates() {
    switch (this.stateProperty().get()) {
      case STATE_ALWAYS:
        this.setState(STATE_IF_TRUE);
        break;
      case STATE_IF_TRUE:
        this.setState(STATE_IF_FALSE);
        break;
      case STATE_IF_FALSE:
        this.setState(STATE_NEVER);
        break;
      case STATE_NEVER:
        this.setState(STATE_ALWAYS);
        break;
    }
  }

  /* get file */
  public Path getSourceFile() {
    return _file;
  }

}
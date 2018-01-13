/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RPC;

import static de.dfki.mlt.rudibugger.RPC.LogData.*;

import java.util.HashMap;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class LabelCellFactory extends TableCell<LogData, LogData.StringPart> {

  public static HashMap<Integer, Color> colourMap
          = new HashMap<Integer, Color>() {{
      put(RED, Color.RED);
      put(GREEN, Color.GREEN);
      put(GRAY, Color.GRAY);
      put(BLACK, Color.BLACK);
    }};

  @Override
  protected void updateItem(LogData.StringPart item, boolean empty) {
    super.updateItem(item, empty);

    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      TextFlow textFlow = new TextFlow();
      Text t = new Text(item.content);
        t.setFill(colourMap.get(item.colour));
        textFlow.getChildren().add(t);
        setGraphic(textFlow);
      }
    }
  }
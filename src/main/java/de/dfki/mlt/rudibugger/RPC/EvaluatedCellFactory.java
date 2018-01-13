/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RPC;

import static de.dfki.mlt.rudibugger.RPC.LogData.*;
import de.dfki.mlt.rudibugger.RPC.LogData.StringPart;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class EvaluatedCellFactory
        extends TableCell<LogData, ArrayList<StringPart>> {

  public static HashMap<Integer, Color> colourMap
          = new HashMap<Integer, Color>() {{
      put(RED, Color.RED);
      put(GREEN, Color.GREEN);
      put(GRAY, Color.GRAY);
      put(BLACK, Color.BLACK);
    }};

  @Override
  protected void updateItem(ArrayList<LogData.StringPart> item, boolean empty) {
    super.updateItem(item, empty);

    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      TextFlow textFlow = new TextFlow();
      for (LogData.StringPart x : item) {
        Text t = new Text(x.content);
        t.setFill(colourMap.get(x.colour));
        textFlow.getChildren().add(t);
      }
      setGraphic(textFlow);
    }
  }
}

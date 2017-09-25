/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.tabs;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Scanner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiTab extends Tab {

  /* the logger */
  static Logger log = Logger.getLogger("rudiLog");

  /**
   * creates a new empty tab with codeArea
   * e.g. new file, maybe as starting point after startup
   */
  public RudiTab(TabPane tabpane) throws FileNotFoundException {
    this(tabpane, null);
  }

  /**
   * creates a new tab with codeArea reading in a file (if provided)
   */
  public RudiTab(TabPane tabpane, Path file) throws FileNotFoundException {
    super();

    /* create new tab and add it to tabpane */
    Tab tab = new Tab("Tab " + (tabpane.getTabs().size() + 1));
    tabpane.getTabs().add(tab);
    tabpane.getSelectionModel().select(tab);

    /* give the tab a name */
    if (file == null) {
      tab.setText("Untitled " +  tab.getText());
    } else {
      tab.setText(file.getFileName().toString());
    }

    /* create a CodeArea */
    RudiCodeArea codeArea = new RudiCodeArea();
    codeArea.initializeCodeArea();

    /* add Scrollbar to tab's content */
    VirtualizedScrollPane textAreaWithScrollBar = new VirtualizedScrollPane<>(codeArea);

    /* set TextArea to fit parent VirtualizedScrollPane */
    AnchorPane.setTopAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane.setRightAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane.setLeftAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane.setBottomAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane content = new AnchorPane(textAreaWithScrollBar);

    /* set css */
    try {
      content.getStylesheets().add("/styles/java-keywords.css");
    } catch (NullPointerException e) {
      log.fatal("The provided css file could not be found.");
    }

    /* read in file (if provided) */
    if (file != null) {
      Scanner s = new Scanner(file.toFile()).useDelimiter("\n");
      while (s.hasNext()) {
        codeArea.appendText(s.next() + "\n");
      }
    }

    /* load content into tab */
    tab.setContent(content);
  }


}

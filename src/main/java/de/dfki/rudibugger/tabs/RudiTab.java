/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.tabs;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Scanner;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
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

  /* the associated file */
  private Path _file;

  /* the codeArea */
  public RudiCodeArea _codeArea;

  /* creates a new empty tab */
  public RudiTab() {
    super();
    this.setOnCloseRequest((Event arg0) -> {
      ((RudiHBox) this.getTabPane().getParent()).removeTabFromOpenTabs(this);
    });
  }

  /* creates a new tab and links a file to it */
  public RudiTab(Path file) {
    this();
    _file = file;
  }

  public void setContent() {

    /* set the title of the tab */
    if (_file == null) {
      this.setText("Untitled RudiTab");
    } else {
      this.setText(_file.getFileName().toString());
    }

    /* create a CodeArea */
    _codeArea = new RudiCodeArea();
    _codeArea.initializeCodeArea();

    /* add Scrollbar to tab's content */
    VirtualizedScrollPane textAreaWithScrollBar
            = new VirtualizedScrollPane<>(_codeArea);

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
    if (_file != null) {
      try {
        Scanner s = new Scanner(_file.toFile()).useDelimiter("\n");
        while (s.hasNext()) {
          _codeArea.appendText(s.next() + "\n");
        }
      } catch (FileNotFoundException e) {
        log.error("Something went wrong while reading in " + _file.getFileName().toString());
      }
    }

    /* set the shown part of the file */
    _codeArea.showParagraphAtTop(0);
    _codeArea.moveTo(0, 0);

    /* load content into tab */
    this.setContent(content);

  }

  public Path getFile() {
    return _file;
  }
}

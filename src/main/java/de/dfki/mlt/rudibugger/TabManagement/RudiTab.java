/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.TabManagement;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Scanner;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiTab extends Tab {

  /** the logger */
  static Logger log = LoggerFactory.getLogger("rudiLog");

  /** the associated file */
  private Path _file;

  /** the codeArea */
  protected RudiCodeArea _codeArea;

  /** is this a known file or a new one? */
  private Boolean isKnown;

  /* creates a new empty tab */
  public RudiTab() {
    super();
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
      content.getStylesheets().add("/styles/rudi-keywords.css");
    } catch (NullPointerException e) {
      log.error("The provided css file could not be found.");
    }

    /* read in file (if provided) */
    if (_file != null) {
      try {
        Scanner s = new Scanner(_file.toFile()).useDelimiter("\n");
        while (s.hasNext()) {
          _codeArea.appendText(s.next() + "\n");
        }
      } catch (FileNotFoundException e) {
        log.error("Something went wrong while reading in "
                + _file.getFileName().toString());
      }
      isKnown = true;
    } else {
      isKnown = false;
    }

    /* set the shown part of the file */
    _codeArea.showParagraphAtTop(0);
    _codeArea.moveTo(0, 0);

    /* load content into tab */
    this.setContent(content);

    /* wait for modifications */
    waitForModif();

  }

  private final BooleanProperty hasBeenModified = new SimpleBooleanProperty();
  public BooleanProperty hasBeenModifiedProperty() { return hasBeenModified; }

  public void waitForModif() {
    hasBeenModified.setValue(false);
    _codeArea.textProperty().addListener(cl);
  }

  private ChangeListener cl = (ChangeListener) new ChangeListener() {
    @Override
    public void changed(ObservableValue o, Object oldValue, Object newValue) {
      setText("*" + RudiTab.this.getText());
      hasBeenModified.setValue(true);
      o.removeListener(cl);
    }
  };

  /**
   * Indicates that the file is not a new, unsaved file
   *
   * @return
   */
  public Boolean isKnown() {
    return isKnown;
  }

  /**
   * Returns the file this tab is associated to
   *
   * @return
   */
  public Path getFile() {
    return _file;
  }

  /**
   * Returns the .rudi code shown in the tab
   *
   * @return
   */
  public String getRudiCode() {
    return _codeArea.getText();
  }


}

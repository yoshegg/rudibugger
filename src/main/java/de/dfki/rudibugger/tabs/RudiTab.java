/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.rudibugger.tabs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiTab extends Tab {

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
  public RudiTab(TabPane tabpane, File file) throws FileNotFoundException {

    // create new tab and add it to tabpane
    Tab tab = new Tab("Tab " + (tabpane.getTabs().size() + 1));
    tabpane.getTabs().add(tab);
    tabpane.getSelectionModel().select(tab);

    // create a CodeArea
    CodeArea codeArea = new CodeArea();
    codeArea.setWrapText(false); // does not work well
    codeArea.setParagraphGraphicFactory(
            LineNumberFactory.get(codeArea, digits -> "%"+ (digits < 2 ? 2 : digits) + "d"));

    // add Scrollbar to tab's content
    VirtualizedScrollPane textAreaWithScrollBar = new VirtualizedScrollPane<>(codeArea);

    // set TextArea to fit parent VirtualizedScrollPane
    AnchorPane.setTopAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane.setRightAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane.setLeftAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane.setBottomAnchor(textAreaWithScrollBar, 0.0);
    AnchorPane content = new AnchorPane(textAreaWithScrollBar);

    // read in file (if provided)
    // https://stackoverflow.com/questions/27222205/javafx-read-from-text-file-and-display-in-textarea
    if (file != null) {
      Scanner s = new Scanner((File) file).useDelimiter("\n");
      while (s.hasNext()) {
        codeArea.appendText(s.next() + "\n");
      }
    }

    // load content into tab
    tab.setContent(content);
  }


}

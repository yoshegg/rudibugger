package de.dfki.rudibugger;

import de.dfki.rudibugger.folderstructure.RudiTreeItem;
import de.dfki.rudibugger.tabs.RudiTab;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class FXMLController implements Initializable {

  boolean testing = true;

  @FXML
  private Label statusbar;

  @FXML
  private TabPane tabpanex;

  @FXML
  private TreeView treeviewx;

  @FXML
  private void handleButtonAction(ActionEvent event) {
    System.out.println("You clicked me!");
    if (testing) {
      statusbar.setText("Hello World!");
      testing = false;
    } else {
      statusbar.setText("Testing...");
      testing = true;
    }
  }

  @FXML
  private void openNewTab(ActionEvent event) throws FileNotFoundException {
    RudiTab tab = new RudiTab(tabpanex);
  }

  @FXML
  private void tabClicked(MouseEvent event) {
    System.out.println("tab clicked!");

    if (event.getButton() == MouseButton.MIDDLE) {
      System.out.println("Middle mouse button clicked!");
      Tab selectedTab = tabpanex.getSelectionModel().getSelectedItem();
      System.out.println(selectedTab);
    }
  }

  @FXML
  private void closeApplication(ActionEvent event) {
    MainApp.exitRudibugger();
  }

  @FXML
  private void openFile(ActionEvent event) throws FileNotFoundException {
    MainApp.getInstance().openFile(tabpanex);
  }

  @FXML
  private void openProject(ActionEvent event) {
    MainApp.getInstance().openProject(treeviewx);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    tabpanex.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);

    // https://stackoverflow.com/questions/17348357/how-to-trigger-event-when-double-click-on-a-tree-node
    treeviewx.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
          Object test = treeviewx.getSelectionModel().getSelectedItem();
          if (test instanceof RudiTreeItem) {
            RudiTreeItem item = (RudiTreeItem) test;
            System.out.println("Selected Text : " + item.getValue());
            RudiTab tabdata;
            try {
              tabdata = new RudiTab(tabpanex, item.getFile());
            } catch (FileNotFoundException ex) {
              Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
        }
      }
    });
  }
}

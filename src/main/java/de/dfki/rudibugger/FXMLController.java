package de.dfki.rudibugger;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

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
  private void openNewTab(ActionEvent event) {
    System.out.println("new Tab!");
    Tab tab = new Tab("Tab " + (tabpanex.getTabs().size() + 1));
    tabpanex.getTabs().add(tab);
    tabpanex.getSelectionModel().select(tab);
    
    AnchorPane content = new AnchorPane();     
    TextArea textArea = new TextArea();
    
    // set TextArea to fit parent Anchor Pane
    AnchorPane.setTopAnchor(textArea, 0.0);
    AnchorPane.setRightAnchor(textArea, 0.0);
    AnchorPane.setLeftAnchor(textArea, 0.0);
    AnchorPane.setBottomAnchor(textArea, 0.0);
    //textArea.setId("test");
    content.getChildren().add(textArea);
    tab.setContent(content);
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
  private void openFile(ActionEvent event) {
    MainApp.getInstance().openFile();
  }
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    tabpanex.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
  }  
  
}

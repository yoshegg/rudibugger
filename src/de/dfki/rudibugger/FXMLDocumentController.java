/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class FXMLDocumentController implements Initializable {
  
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
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    tabpanex.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
  }  
  
}

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

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class FXMLDocumentController implements Initializable {
  
  boolean testing = true;
  
  @FXML
  private Label statusbar;
  
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
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    // TODO
  }  
  
}

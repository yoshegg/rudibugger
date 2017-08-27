/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.rudibugger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Rudibugger extends Application {
  
  Stage stageX;
  
  @Override
  public void start(Stage stage) throws Exception {
    stageX = stage;
    Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
    Scene scene = new Scene(root);
    
    stage.setScene(scene);
    stage.show();
  }

  
  private static Rudibugger instance;

  public Rudibugger() {
    instance = this;
  }

  public static Rudibugger getInstance() {
    return instance;
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
  
  public void openFile() {
    FileChooser fileChooser = new FileChooser();
    
    //Set extension filter
    FileChooser.ExtensionFilter extFilterRUDI = new FileChooser.ExtensionFilter("RUDI files (*.rudi)", "*.rudi", "*.RUDI");
    FileChooser.ExtensionFilter extFilterALL = new FileChooser.ExtensionFilter("All files", "*.*");
    fileChooser.getExtensionFilters().addAll(extFilterRUDI);
    fileChooser.getExtensionFilters().addAll(extFilterALL);
    
    fileChooser.setTitle("Open .rudi file");
    fileChooser.showOpenMultipleDialog(stageX);
  }
  
  public static void exitRudibugger() {
    Platform.exit();
  }
  
}

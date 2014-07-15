package org.peerbox.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

public class SelectRootPathController implements Initializable{

	@FXML
	private Button changeDirButton;
	@FXML
	private Button okButton;
	@FXML
	private Button goBackButton;
	@FXML
	private TextField pathTextField;
	
	
	public void changeDirectory(ActionEvent event){
		DirectoryChooser chooser = new DirectoryChooser();
	    chooser.setTitle("Choose your root directory");
	    chooser.setInitialDirectory(new File(pathTextField.getText()));
	    File selectedDirectory = chooser.showDialog(okButton.getScene().getWindow());
	    if (selectedDirectory != null) {
	        pathTextField.setText(selectedDirectory.getAbsolutePath());
	    }  
	}
	
	public void goBack(ActionEvent event){
		MainNavigator.navigate("../CreateNetworkWindow.fxml");
	}
	
	public void openLoginView(ActionEvent event){
		System.out.println("Go back.");
		MainNavigator.goBack();
	}


	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		pathTextField.setText("C:/Users");
		pathTextField.setPrefWidth(250);
	}
}
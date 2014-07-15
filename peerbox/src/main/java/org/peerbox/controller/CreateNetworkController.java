package org.peerbox.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CreateNetworkController implements Initializable {

	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

	}
	
	@FXML
	private Button btnBackToSelection;
	
	@FXML
	private TextField ipOutputAddress;
	
	public void goBackToSelection(ActionEvent event){
		System.out.println("Go back.");	
		MainNavigator.goBack();
	}

}

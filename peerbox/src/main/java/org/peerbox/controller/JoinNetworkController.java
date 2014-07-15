package org.peerbox.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public class JoinNetworkController implements Initializable {

	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

	}
	
	@FXML
	private Button btnBackToSelection;
	
	public void goBackToSelection(ActionEvent event){
		System.out.println("Network created.");	
		MainNavigator.navigate("../NetworkSelectionWindow.fxml");
	}

}

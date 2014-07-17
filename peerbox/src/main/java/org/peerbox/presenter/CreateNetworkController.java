package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import org.peerbox.model.H2HManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CreateNetworkController implements Initializable {

	@FXML
	private Button btnBackToSelection;
	
	@FXML
	private TextField ipOutputAddress;
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		if(H2HManager.INSTANCE.getNode() == null){
			System.out.println("Try to create network...");
			H2HManager.INSTANCE.createNode();
			ipOutputAddress.setText(H2HManager.INSTANCE.getInetAddressAsString());
		}
		
	}
	
	public void goBack(ActionEvent event){
		System.out.println("Go back.");	
		MainNavigator.goBack();
	}
	
	public void createNetwork(ActionEvent event){
		MainNavigator.navigate("/org/peerbox/view/RegisterView.fxml");
	}

}

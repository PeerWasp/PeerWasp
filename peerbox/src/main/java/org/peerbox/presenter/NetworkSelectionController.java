package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

public class NetworkSelectionController implements Initializable {

	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

	}
	
	
	public void createNetwork(ActionEvent event){
		System.out.println("Network created.");	
		MainNavigator.navigate("/org/peerbox/view/CreateNetworkWindow.fxml");
	}

	public void joinNetwork(ActionEvent event){
		System.out.println("Network joined.");	
		MainNavigator.navigate("/org/peerbox/view/JoinNetworkWindow.fxml");
	}
}

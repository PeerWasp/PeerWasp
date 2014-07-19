package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import org.peerbox.view.ViewNames;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

public class NetworkSelectionController implements Initializable {

	public void initialize(URL arg0, ResourceBundle arg1) {

	}
	
	
	public void createNetwork(ActionEvent event){
		System.out.println("Network created.");	
		MainNavigator.navigate(ViewNames.CREATE_NETWORK_VIEW);
	}

	public void joinNetwork(ActionEvent event){
		System.out.println("Network joined.");	
		MainNavigator.navigate(ViewNames.JOIN_NETWORK_VIEW);
	}
}

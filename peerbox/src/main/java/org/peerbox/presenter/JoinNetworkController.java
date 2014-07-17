package org.peerbox.presenter;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import org.peerbox.model.H2HManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class JoinNetworkController implements Initializable {

	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

	}
	
	@FXML
	private TextField txtBootstrapIP;
	
	public void goBack(ActionEvent event){
		System.out.println("Go back.");
		MainNavigator.goBack();
	}
	
	public void accessNetwork(ActionEvent event){
		System.out.println("Try to join network at provided IP address.");
		try {
			if(H2HManager.INSTANCE.accessNetwork(txtBootstrapIP.getText())){

				MainNavigator.navigate("/org/peerbox/view/SelectRootPathView.fxml");
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

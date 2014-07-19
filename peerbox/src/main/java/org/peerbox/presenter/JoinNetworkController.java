package org.peerbox.presenter;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import org.peerbox.model.H2HManager;

import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class JoinNetworkController implements Initializable {

	private H2HManager h2hManager;
	
	@Inject
	public JoinNetworkController(H2HManager h2hManager) {
		this.h2hManager = h2hManager;
	}
	
	public void initialize(URL arg0, ResourceBundle arg1) {

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
			
			if(h2hManager.accessNetwork(txtBootstrapIP.getText())){
				if(h2hManager.getRootPath().toString().equals("unset")){
					MainNavigator.navigate("/org/peerbox/view/SelectRootPathView.fxml");
				} else {
					MainNavigator.navigate("/org/peerbox/view/LoginView.fxml");
				}
				
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

package org.peerbox.presenter;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.peerbox.model.H2HManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class JoinNetworkController implements Initializable {

	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

	}
	
	@FXML
	private Button btnBackToSelection;
	
	@FXML
	private TextField bootstrapAddressField;
	
	public void goBack(ActionEvent event){
		System.out.println("Go back.");
		MainNavigator.goBack();
	}
	
	public void accessNetwork(ActionEvent event){
		System.out.println("Try to join network at provided IP address.");
		try {
			String nodeID = H2HManager.INSTANCE.generateNodeID();
			InetAddress bootstrapAddress = InetAddress.getByName(bootstrapAddressField.getText());
			H2HManager.INSTANCE.createNode(NetworkConfiguration.create(nodeID, bootstrapAddress));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(H2HManager.INSTANCE.getNode().connect()){
			System.out.println("Joined the network.");
			MainNavigator.navigate("/org/peerbox/view/SelectRootPathView.fxml");
		} else {
			System.out.println("Was not able to join network!");
		}

	}

}

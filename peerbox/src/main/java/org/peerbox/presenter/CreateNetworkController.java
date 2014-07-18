package org.peerbox.presenter;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.peerbox.model.H2HManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CreateNetworkController implements Initializable {

	@FXML
	private Button btnBack;
	
	@FXML
	private Button btnCreate;
	
	@FXML
	private TextField txtIPAddress;
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			txtIPAddress.setText(InetAddress.getLocalHost().getHostAddress().toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void goBack(ActionEvent event){
		Action goBack = Dialog.Actions.YES;
		if(H2HManager.INSTANCE.getNode() != null && H2HManager.INSTANCE.getNode().isConnected()){
			goBack = Dialogs.create()
			      .title("Delete the network?")
			      .message("If you go back, your peer will be shut down "
			      		+ "and your network deleted. Continue?")
			      .showConfirm();
		}
		if(goBack.equals(Dialog.Actions.YES)){
			H2HManager.INSTANCE.disconnectNode();
			btnCreate.setText("Create");
			MainNavigator.goBack();
		}
	}
	
	public void createNetwork(ActionEvent event){
		if(H2HManager.INSTANCE.getNode() == null || 
				!H2HManager.INSTANCE.getNode().isConnected()){
			
			H2HManager.INSTANCE.createNode();
			Dialogs.create().title("New network created!")
				.message("The bootstrapping peer on " + txtIPAddress.getText() + " is started.")
				.showInformation();
			btnCreate.setText("Continue");
		}
		MainNavigator.navigate("/org/peerbox/view/RegisterView.fxml");
	}
}

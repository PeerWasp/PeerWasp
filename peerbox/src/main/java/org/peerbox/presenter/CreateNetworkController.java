package org.peerbox.presenter;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.peerbox.model.H2HManager;
import org.peerbox.view.ViewNames;

import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CreateNetworkController implements Initializable {

	private H2HManager h2hManager;
	private NavigationService fNavigationService;
	
	@FXML
	private Button btnBack;
	
	@FXML
	private Button btnCreate;
	
	@FXML
	private TextField txtIPAddress;
	
	@Inject
	public CreateNetworkController(NavigationService navigationService, H2HManager h2hManager) {
		this.fNavigationService = navigationService;
		this.h2hManager = h2hManager;
	}
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			txtIPAddress.setText(InetAddress.getLocalHost().getHostAddress().toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void goBack(ActionEvent event){
		Action goBack = Dialog.Actions.YES;
		if(h2hManager.getNode() != null && h2hManager.getNode().isConnected()){
			goBack = Dialogs.create()
			      .title("Delete the network?")
			      .message("If you go back, your peer will be shut down "
			      		+ "and your network deleted. Continue?")
			      .showConfirm();
		}
		if(goBack.equals(Dialog.Actions.YES)){
			h2hManager.disconnectNode();
			btnCreate.setText("Create");
			fNavigationService.goBack();
		}
	}
	
	public void createNetwork(ActionEvent event){
		if(h2hManager.getNode() == null || 
				!h2hManager.getNode().isConnected()){
			
			h2hManager.createNode();
			Dialogs.create().title("New network created!")
				.message("The bootstrapping peer on " + txtIPAddress.getText() + " is started.")
				.showInformation();
			btnCreate.setText("Continue");
		}
		fNavigationService.navigate(ViewNames.REGISTER_VIEW);
	}
}

package org.peerbox.presenter;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import org.peerbox.model.H2HManager;
import org.peerbox.view.ViewNames;
import org.peerbox.view.controls.ErrorLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class CreateNetworkController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(CreateNetworkController.class);
			
	private H2HManager h2hManager;
	private NavigationService fNavigationService;
	
	@FXML
	private Button btnCreate;
	@FXML
	private TextField txtIPAddress;
	@FXML
	private ErrorLabel lblError;
	
	@Inject
	public CreateNetworkController(NavigationService navigationService, H2HManager h2hManager) {
		this.fNavigationService = navigationService;
		this.h2hManager = h2hManager;
	}
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			txtIPAddress.setText(InetAddress.getLocalHost().getHostAddress().toString());
		} catch (UnknownHostException e) {
			logger.warn("Could not determine address of host (Exception: {})", e.getMessage());
			setError("Could not determine address of host.");
		}
	}
	
	public void navigateBackAction(ActionEvent event) {
		boolean goBack = true;
		clearError();
		if (h2hManager.isConnected()) {
			goBack = showConfirmDeleteNetworkDialog();
		}
		if (goBack) {
			h2hManager.leaveNetwork();
			btnCreate.setText("Create");
			logger.debug("Navigate back.");
			fNavigationService.navigateBack();
		}
	}

	private boolean showConfirmDeleteNetworkDialog() {
		boolean yes = false;
		
		Window owner = txtIPAddress.getScene().getWindow();
		Alert dlg = new Alert(AlertType.CONFIRMATION);
		dlg.initOwner(owner);
		dlg.setTitle("Delete Network");
		dlg.setHeaderText("Delete the network?");
		dlg.setContentText("If you go back, your peer will be shut down and your network deleted. Continue?");
		dlg.showAndWait();
		
		yes = dlg.getResult() == ButtonType.OK;

		return yes;
	}
	
	public void createNetworkAction(ActionEvent event) {
		clearError();
		if (!h2hManager.isConnected()) {
			if (h2hManager.createNetwork()) {
				btnCreate.setText("Continue");
				logger.debug("Network created (Host address: {})", txtIPAddress.getText());
			} else {
				setError("Could not create network.");
				logger.error("Could not create network (createNode returned false).");
			}
		}
		fNavigationService.navigate(ViewNames.REGISTER_VIEW);
	}

	private void showNetworkCreatedDialog() {
		Window owner = txtIPAddress.getScene().getWindow();
		Alert dlg = new Alert(AlertType.INFORMATION);
		dlg.initOwner(owner);
		dlg.setTitle("Network Created");
		dlg.setHeaderText("New network created");
		dlg.setContentText(String.format("The bootstrapping peer started on %s.", txtIPAddress.getText()));
		dlg.showAndWait();
	}
	
	private void setError(String error) {
		lblError.setText(error);
	}

	private void clearError() {
		lblError.setText("");
	}
}

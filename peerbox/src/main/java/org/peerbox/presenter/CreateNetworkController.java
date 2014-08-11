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
import org.peerbox.view.controls.ErrorLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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
		Action goBack = Dialog.Actions.YES;
		clearError();
		if (h2hManager.isConnected()) {
			goBack = showConfirmDeleteNetworkDialog();
		}
		if (goBack.equals(Dialog.Actions.YES)) {
			h2hManager.leaveNetwork();
			btnCreate.setText("Create");
			logger.debug("Navigate back.");
			fNavigationService.navigateBack();
		}
	}

	private Action showConfirmDeleteNetworkDialog() {
		return Dialogs
				.create()
				.actions(Dialog.Actions.YES, Dialog.Actions.NO)
				.title("Delete the network?")
				.message("If you go back, your peer will be shut down "
								+ "and your network deleted. Continue?")
				.showConfirm();
	}
	
	public void createNetworkAction(ActionEvent event) {
		clearError();
		if (!h2hManager.isConnected()) {
			if (h2hManager.createNode()) {
				btnCreate.setText("Continue");
				showNetworkCreatedDialog();
				logger.debug("Network created (Host address: {})", txtIPAddress.getText());
			} else {
				setError("Could not create network.");
				logger.error("Could not create network (createNode returned false).");
			}
		}
		fNavigationService.navigate(ViewNames.REGISTER_VIEW);
	}

	private void showNetworkCreatedDialog() {
		Dialogs.create()
				.title("New network created!")
				.message(String.format("The bootstrapping peer started on %s.",
								txtIPAddress.getText()))
				.showInformation();
	}
	
	private void setError(String error) {
		lblError.setText(error);
	}

	private void clearError() {
		lblError.setText("");
	}
}

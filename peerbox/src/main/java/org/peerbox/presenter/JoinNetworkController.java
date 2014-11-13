package org.peerbox.presenter;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.peerbox.UserConfig;
import org.peerbox.model.H2HManager;
import org.peerbox.presenter.validation.EmptyTextFieldValidator;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;
import org.peerbox.view.ViewNames;
import org.peerbox.view.controls.ErrorLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JoinNetworkController implements Initializable {
	
	private static final Logger logger = LoggerFactory.getLogger(JoinNetworkController.class);

	private H2HManager h2hManager;
	private NavigationService fNavigationService;
	private UserConfig userConfig;

	@FXML
	private VBox vboxForm;
	@FXML
	private TextField txtBootstrapAddress;
	@FXML
	private ComboBox<String> bootstrapNodes;
	@FXML
	private ErrorLabel lblError;
	@FXML
	private Label lblBootstrapAddressError;
	
	private EmptyTextFieldValidator bootstrapValidator;

	@Inject
	public JoinNetworkController(NavigationService navigationService, H2HManager h2hManager) {
		this.fNavigationService = navigationService;
		this.h2hManager = h2hManager;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		Platform.runLater(() -> {
			txtBootstrapAddress.requestFocus();
		});
		initializeValidations();
		initializedBootstrapNodes();
	}

	private void initializedBootstrapNodes() {
		bootstrapNodes.getItems().addAll(userConfig.getBootstrappingNodes());
	}

	private void initializeValidations() {
		bootstrapValidator = new EmptyTextFieldValidator(txtBootstrapAddress, true, ValidationResult.BOOTSTRAPHOST_EMPTY);
		bootstrapValidator.setErrorProperty(lblBootstrapAddressError.textProperty());
	}

	public void navigateBackAction(ActionEvent event) {
		logger.debug("Navigate back.");
		fNavigationService.navigateBack();
	}
	
	public void onBootstrapNodeSelected(ActionEvent event) {
		String selectedNode = bootstrapNodes.getSelectionModel().getSelectedItem();
		txtBootstrapAddress.setText(selectedNode);
	}
	
	public void joinNetworkAction(ActionEvent event) {
		clearError();
		if (validateAll().isError()) {
			return;
		}

		String address = txtBootstrapAddress.getText().trim();
		logger.info("Join network '{}'", address);
		try {
			if (h2hManager.joinNetwork(address)) {
				udpateUserConfig();
				if (!userConfig.hasRootPath()) {
					fNavigationService.navigate(ViewNames.SELECT_ROOT_PATH_VIEW);
				} else {
					fNavigationService.navigate(ViewNames.LOGIN_VIEW);
				}
			} else {
				setError(String.format("Could not connect to '%s'", address));
			}
		} catch (UnknownHostException e) {
			setError(String.format("Could not connect to '%s'", address));
			logger.info("Could not connect to '{}' ({})", address, e.getMessage());
		}
	}
	
	private ValidationResult validateAll() {
		return (bootstrapValidator.validate() == ValidationResult.OK) 
				? ValidationResult.OK : ValidationResult.ERROR;
	}

	private void udpateUserConfig() {
		try {
			userConfig.addBootstrapNode(txtBootstrapAddress.getText().trim());
		} catch (IOException ioex) {
			logger.warn("Could not save settings: {}", ioex.getMessage());
			setError("Could not save settings.");
		}
	}

	private void setError(String error) {
		lblError.setText(error);
	}

	private void clearError() {
		lblError.setText("");
	}

	@Inject
	public void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
	}
}

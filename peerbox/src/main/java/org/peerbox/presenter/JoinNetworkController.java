package org.peerbox.presenter;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jidefx.scene.control.validation.ValidationMode;
import jidefx.scene.control.validation.ValidationUtils;
import jidefx.scene.control.validation.Validator;

import org.peerbox.UserConfig;
import org.peerbox.model.H2HManager;
import org.peerbox.utils.FormValidationUtils;
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
	private TextField txtBootstrapIP;
	@FXML
	private ErrorLabel lblError;

	@Inject
	public JoinNetworkController(NavigationService navigationService, H2HManager h2hManager) {
		this.fNavigationService = navigationService;
		this.h2hManager = h2hManager;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		initializeValidations();
	}

	private void initializeValidations() {
		wrapDecorationPane();

		Validator bootstrapAddrValidator = FormValidationUtils.createEmptyTextFieldValidator(
				txtBootstrapIP, "Please enter an address.", true);
		ValidationUtils.install(txtBootstrapIP, bootstrapAddrValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtBootstrapIP, bootstrapAddrValidator, ValidationMode.ON_DEMAND);
	}

	private void wrapDecorationPane() {
		Pane dp = FormValidationUtils.wrapInDecorationPane((Pane) vboxForm.getParent(), vboxForm);
		AnchorPane.setLeftAnchor(dp, 0.0);
		AnchorPane.setTopAnchor(dp, 0.0);
		AnchorPane.setRightAnchor(dp, 0.0);
	}

	public void navigateBackAction(ActionEvent event) {
		logger.debug("Navigate back.");
		fNavigationService.navigateBack();
	}

	public void joinNetworkAction(ActionEvent event) {
		clearError();
		if (!ValidationUtils.validateOnDemand(vboxForm)) {
			return;
		}

		String address = txtBootstrapIP.getText().trim();
		logger.info("Join network '{}'", address);
		try {
			if (h2hManager.joinNetwork(address)) {
				udpateUserConfig();
				if (!userConfig.rootPathExists()) {
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

	private void udpateUserConfig() {
		try {
			userConfig.addBootstrapNode(txtBootstrapIP.getText().trim());
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

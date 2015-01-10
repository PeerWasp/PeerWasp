package org.peerbox.presenter;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import org.apache.commons.io.FileUtils;
import org.peerbox.UserConfig;
import org.peerbox.presenter.validation.RootPathValidator;
import org.peerbox.presenter.validation.SelectRootPathUtils;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;
import org.peerbox.view.ViewNames;
import org.peerbox.view.controls.ErrorLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


public class SelectRootPathController implements Initializable {
	
	private static final Logger logger = LoggerFactory.getLogger(SelectRootPathController.class);
	private NavigationService fNavigationService;
	private UserConfig userConfig;

	@FXML
	private Button btnChangeRootPath;
	@FXML
	private Button btnContinue;
	@FXML
	private Button btnNavigateBack;
	@FXML
	private TextField txtRootPath;
	@FXML
	private Label lblPathError;
	@FXML
	private ErrorLabel lblError;
	
	private RootPathValidator pathValidator;
	
	@Inject
	public SelectRootPathController(NavigationService navigationService) {
		this.fNavigationService = navigationService;
	}
	
	public void initialize(URL location, ResourceBundle resources) {
		initializePath();
		initializeValidation();
	}

	private void initializeValidation() {
		pathValidator = new RootPathValidator(txtRootPath, lblPathError.textProperty());
	}

	private void initializePath() {
		String defaultDir = "";
		if (userConfig.hasRootPath()) {
			defaultDir = userConfig.getRootPath().toString();
		} else {
			Date now = new Date();
			defaultDir = Paths.get(FileUtils.getUserDirectoryPath(),
					String.format("PeerBox_%s", now.getTime())).toString();
		}
		txtRootPath.setText(defaultDir);
	}

	public void changeRootPathAction(ActionEvent event) {
		String path = txtRootPath.getText();
		Window toOpenDialog = btnContinue.getScene().getWindow();
		path = SelectRootPathUtils.showDirectoryChooser(path, toOpenDialog);
		txtRootPath.setText(path);
	}
	
	public void navigateBackAction(ActionEvent event) {
		logger.debug("Navigate back.");
		clearError();
		fNavigationService.navigateBack();
	}
	
	public void continueAction(ActionEvent event) {
		clearError();
		String path = txtRootPath.getText();
		ValidationResult result = pathValidator.validate();
		if (!result.isError()) {
			try {
				logger.info("Root path set to '{}'", path);
				userConfig.setRootPath(Paths.get(path));
				fNavigationService.navigate(ViewNames.LOGIN_VIEW);
			} catch (IOException e) {
				logger.warn("Could not save settings (Exception: {})", e.getMessage());
				setError("Could not save settings.");
			}
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
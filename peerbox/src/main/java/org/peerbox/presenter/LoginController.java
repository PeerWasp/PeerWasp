package org.peerbox.presenter;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import jidefx.scene.control.decoration.DecorationUtils;
import jidefx.scene.control.decoration.Decorator;
import jidefx.scene.control.validation.ValidationMode;
import jidefx.scene.control.validation.ValidationUtils;
import jidefx.scene.control.validation.Validator;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.peerbox.ResultStatus;
import org.peerbox.UserConfig;
import org.peerbox.model.UserManager;
import org.peerbox.utils.FormValidationUtils;
import org.peerbox.view.ViewNames;
import org.peerbox.view.controls.ErrorLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


public class LoginController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	private NavigationService fNavigationService;
	private UserManager userManager;
	private UserConfig userConfig;

	@FXML
	private TextField txtUsername;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private PasswordField txtPin;
	@FXML
	private TextField txtRootPath;
	@FXML
	private CheckBox chbAutoLogin;
	@FXML
	private Button btnLogin;
	@FXML
	private Button btnRegister;
	@FXML
	private GridPane grdForm;
	@FXML
	private ErrorLabel lblError;

	private Decorator<ProgressIndicator> fProgressDecoration = null;
	
	
	@Inject
	public LoginController(NavigationService navigationService, UserManager userManager) {
		this.fNavigationService = navigationService;
		this.userManager = userManager;
	}
	
	public void initialize(URL location, ResourceBundle resources) {
		initializeValidations();
		loadUserConfig();
	}
	
	private void loadUserConfig() {
		if (userConfig.hasUsername()) {
			txtUsername.setText(userConfig.getUsername());
		}
		if (userConfig.hasRootPath()) {
			txtRootPath.setText(userConfig.getRootPath().toString());
		}
		chbAutoLogin.setSelected(userConfig.isAutoLoginEnabled());
	}

	private void resetForm() {
		loadUserConfig();
		txtPassword.clear();
		txtPin.clear();
		grdForm.disableProperty().unbind();
		grdForm.setDisable(false);
		uninstallProgressIndicator();
		ValidationUtils.validateOnDemand(grdForm);
	}
	
	private void initializeValidations() {
		wrapDecorationPane();
		addUsernameValidation();
		addPasswordValidation();
		addPinValidation();
	}

	private void wrapDecorationPane() {
		Pane dp = FormValidationUtils.wrapInDecorationPane((Pane) grdForm.getParent(), grdForm);
		AnchorPane.setLeftAnchor(dp, 0.0);
		AnchorPane.setTopAnchor(dp, 0.0);
		AnchorPane.setRightAnchor(dp, 0.0);
	}

	private void addUsernameValidation() {
		Validator usernameValidator = FormValidationUtils.createEmptyTextFieldValidator(txtUsername,
				"Please enter a username.", true);
		ValidationUtils.install(txtUsername, usernameValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtUsername, usernameValidator, ValidationMode.ON_DEMAND);
	}

	private void addPasswordValidation() {
		Validator passwordValidator = FormValidationUtils.createEmptyTextFieldValidator(
				txtPassword, "Please enter a password.", false);
		ValidationUtils.install(txtPassword, passwordValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtPassword, passwordValidator, ValidationMode.ON_DEMAND);
	}

	private void addPinValidation() {
		Validator pinValidator = FormValidationUtils.createEmptyTextFieldValidator(
				txtPin, "Please enter a PIN.", false);
		ValidationUtils.install(txtPin, pinValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtPin, pinValidator, ValidationMode.ON_DEMAND);
		
	}

	public void loginAction(ActionEvent event) {
		boolean inputValid = false;
		try {
			clearError();
			inputValid = ValidationUtils.validateOnDemand(grdForm)
					&& SelectRootPathUtils.verifyRootPath(txtRootPath.getText()) 
					&& checkUserExists();
		} catch (NoPeerConnectionException e) {
			setError("Connection to the network failed.");
		}

		if (inputValid) {
			Task<ResultStatus> task = createLoginTask();
			new Thread(task).start();
		}
	}
	
	
	private boolean checkUserExists() throws NoPeerConnectionException {
		String username = txtUsername.getText().trim();
		if (!userManager.isRegistered(username)) {
			setError("This user profile does not exist.");
			return false;
		}
		return true;
	}

	public ResultStatus loginUser(final String username, final String password, 
			final String pin, final Path path) {
		try {
			return userManager.loginUser(username, password, pin, path);
		} catch (NoPeerConnectionException e) {
			return ResultStatus.error("Could not login user because connection to network failed.");
		} catch (InvalidProcessStateException | InterruptedException e) {
			e.printStackTrace();
		}
		return ResultStatus.error("Could not login user.");
	}

	private Task<ResultStatus> createLoginTask() {
		Task<ResultStatus> task = new Task<ResultStatus>() {
			// credentials
			final String username = getUsername();
			final String password = txtPassword.getText();
			final String pin = txtPin.getText();
			final Path path = Paths.get(txtRootPath.getText().trim());

			@Override
			public ResultStatus call() {
				return loginUser(username, password, pin, path);
			}
		};

		task.setOnScheduled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				grdForm.disableProperty().bind(task.runningProperty());
				installProgressIndicator();
			}
		});

		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				onLoginFailed(ResultStatus.error("Could not login user."));
			}
		});

		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				ResultStatus result = task.getValue();
				if (result.isOk()) {
					onLoginSucceeded();
				} else {
					onLoginFailed(result);
				}
			}
		});

		return task;
	}
	
	private void onLoginFailed(ResultStatus result) {
		logger.error("Login task failed: {}", result.getErrorMessage());
		Platform.runLater(() -> {
			uninstallProgressIndicator();
			grdForm.disableProperty().unbind();
			grdForm.requestLayout();
			setError(result.getErrorMessage());
		});
	}
	
	private void onLoginSucceeded() {
		logger.debug("Login task succeeded: user {} logged in.", getUsername());
		saveLoginConfig();
		resetForm();
		fNavigationService.navigate(ViewNames.SETUP_COMPLETED_VIEW);
	}
	
	private void saveLoginConfig() {
		try {
			userConfig.setUsername(getUsername());
			userConfig.setRootPath(txtRootPath.getText());
			if (chbAutoLogin.isSelected()) {
				userConfig.setPassword(txtPassword.getText());
				userConfig.setPin(txtPin.getText());
				userConfig.setAutoLogin(true);
			} else {
				userConfig.setPassword("");
				userConfig.setPin("");
				userConfig.setAutoLogin(false);
			}
		} catch (IOException ioex) {
			logger.warn("Could not save login settings: {}", ioex.getMessage());
			setError("Could not save login settings.");
		}
	}

	private void installProgressIndicator() {
		ProgressIndicator piProgress = new ProgressIndicator();
		fProgressDecoration = new Decorator<>(piProgress, Pos.CENTER);
		DecorationUtils.install(grdForm, fProgressDecoration);
	}

	private void uninstallProgressIndicator() {
		if (fProgressDecoration != null) {
			DecorationUtils.uninstall(grdForm, fProgressDecoration);
			fProgressDecoration = null;
		}
	}

	public void registerAction(ActionEvent event) {
		logger.debug("Navigate to register view.");
		fNavigationService.navigate(ViewNames.REGISTER_VIEW);
	}

	public void changeRootPathAction(ActionEvent event) {
		String path = txtRootPath.getText();
		Window toOpenDialog = grdForm.getScene().getWindow();
		path = SelectRootPathUtils.showDirectoryChooser(path, toOpenDialog);
		txtRootPath.setText(path);
	}

	public void navigateBackAction(ActionEvent event) {
		logger.debug("Navigate back.");
		fNavigationService.navigateBack();
	}

	private String getUsername() {
		return txtUsername.getText().trim();
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

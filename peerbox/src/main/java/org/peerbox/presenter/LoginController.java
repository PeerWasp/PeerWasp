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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.peerbox.ResultStatus;
import org.peerbox.UserConfig;
import org.peerbox.app.ClientContext;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.guice.provider.ClientContextProvider;
import org.peerbox.presenter.validation.EmptyTextFieldValidator;
import org.peerbox.presenter.validation.RootPathValidator;
import org.peerbox.presenter.validation.SelectRootPathUtils;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;
import org.peerbox.view.ViewNames;
import org.peerbox.view.controls.ErrorLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LoginController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	private NavigationService fNavigationService;
	private IUserManager userManager;
	private UserConfig userConfig;
	@Inject
	private ClientContextProvider clientContext;

	@FXML
	private TextField txtUsername;
	@FXML
	private Label lblUsernameError;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private Label lblPasswordError;
	@FXML
	private PasswordField txtPin;
	@FXML
	private Label lblPinError;
	@FXML
	private TextField txtRootPath;
	@FXML
	private Label lblPathError;
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
	@FXML
	private ProgressIndicator piProgress;

	private EmptyTextFieldValidator usernameValidator;
	private EmptyTextFieldValidator passwordValidator;
	private EmptyTextFieldValidator pinValidator;
	private RootPathValidator pathValidator;

	@Inject
	public LoginController(NavigationService navigationService, IUserManager userManager) {
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
		uninstallValidationDecorations();
	}

	private void initializeValidations() {
		usernameValidator = new EmptyTextFieldValidator(txtUsername, true, ValidationResult.USERNAME_EMPTY);
		usernameValidator.setErrorProperty(lblUsernameError.textProperty());
		passwordValidator = new EmptyTextFieldValidator(txtPassword, false, ValidationResult.PASSWORD_EMPTY);
		passwordValidator.setErrorProperty(lblPasswordError.textProperty());
		pinValidator = new EmptyTextFieldValidator(txtPin, false, ValidationResult.PIN_EMPTY);
		pinValidator.setErrorProperty(lblPinError.textProperty());
		pathValidator = new RootPathValidator(txtRootPath, lblPathError.textProperty());
	}

	private void uninstallValidationDecorations() {
		usernameValidator.reset();
		passwordValidator.reset();
		pinValidator.reset();
		pathValidator.reset();
	}

	public void loginAction(ActionEvent event) {
		boolean inputValid = false;
		try {
			clearError();
			ValidationResult validationRes = validateAll();
			inputValid = !validationRes.isError() && checkUserExists();
		} catch (NoPeerConnectionException e) {
			inputValid = false;
			setError("Connection to the network failed.");
		}

		if (inputValid) {
			Task<ResultStatus> task = createLoginTask();
			new Thread(task).start();
		}
	}

	private ValidationResult validateAll() {
		return (usernameValidator.validate() == ValidationResult.OK
				& passwordValidator.validate() == ValidationResult.OK
				& pinValidator.validate() == ValidationResult.OK
				& pathValidator.validate() == ValidationResult.OK
				) ? ValidationResult.OK : ValidationResult.ERROR;
	}


	private boolean checkUserExists() throws NoPeerConnectionException {
		String username = txtUsername.getText().trim();
		if (!userManager.isRegistered(username)) {
			setError("This user profile does not exist.");
			return false;
		}
		return true;
	}

	public ResultStatus loginUser(final String username, final String password, final String pin,
			final Path path) {
		try {
			return userManager.loginUser(username, password, pin, path);
		} catch (NoPeerConnectionException e) {
			return ResultStatus.error("Could not login user because connection to network failed.");
		}
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

		initializeServices();

	    resetForm();
		hideWindow();
	}

	private void initializeServices() {
		try {

			ClientContext ctx = clientContext.get();
			ctx.getActionExecutor().start();

			// register for local/remote events
			ctx.getFolderWatchService().addFileEventListener(ctx.getFileEventManager());
			ctx.getNodeManager().getNode().getFileManager().subscribeFileEvents(ctx.getFileEventManager());

			ctx.getFolderWatchService().start(userConfig.getRootPath());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveLoginConfig() {
		try {
			userConfig.setUsername(getUsername());
			userConfig.setRootPath(Paths.get(txtRootPath.getText()));
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
		Platform.runLater(() -> {
			// center indicator with respect to the grid
			double xOffset = piProgress.getWidth() / 2.0;
			double yOffset = piProgress.getHeight() / 2.0;
			double x = grdForm.getWidth() / 2.0 - xOffset;
			double y = grdForm.getHeight() / 2.0 - yOffset;
			piProgress.relocate(x, y);
			// show
			piProgress.setVisible(true);
		});
	}

	private void uninstallProgressIndicator() {
		Platform.runLater(() -> {
			piProgress.setVisible(false);
		});
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

	private void hideWindow() {
		Stage stage = (Stage) grdForm.getScene().getWindow();
		stage.close();
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

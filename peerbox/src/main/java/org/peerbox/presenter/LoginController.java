package org.peerbox.presenter;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.peerbox.ResultStatus;
import org.peerbox.app.AppContext;
import org.peerbox.app.ClientContext;
import org.peerbox.app.ClientContextFactory;
import org.peerbox.app.Constants;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.forcesync.ForceSync;
import org.peerbox.forcesync.ListSync;
import org.peerbox.presenter.validation.EmptyTextFieldValidator;
import org.peerbox.presenter.validation.RootPathValidator;
import org.peerbox.presenter.validation.SelectRootPathUtils;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;
import org.peerbox.utils.UserConfigUtils;
import org.peerbox.view.ViewNames;
import org.peerbox.view.controls.ErrorLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LoginController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	private final NavigationService fNavigationService;
	private final AppContext appContext;
	private final IUserManager userManager;

	@Inject
	private ClientContextFactory clientContextFactory;

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
	private HBox boxRootPath;
	@FXML
	private TextField txtRootPath;
	@FXML
	private Button btnRootPath;
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


	private Map<String, UserConfig> availableConfigFiles;
	private final BooleanProperty disableRootPathProperty;

	@Inject
	public LoginController(NavigationService navigationService, AppContext appContext, IUserManager userManager) {
		this.fNavigationService = navigationService;
		this.appContext = appContext;
		this.userManager = userManager;

		this.disableRootPathProperty = new SimpleBooleanProperty(false);
	}

	public void initialize(URL location, ResourceBundle resources) {
		initializeValidations();
		loadUserConfig();
	}

	private void loadUserConfig() {
		// allow or prevent that user can select / edit root path
		boxRootPath.disableProperty().bind(disableRootPathProperty);

		// read config for user and set root path if config file found
		availableConfigFiles = UserConfigUtils.getAllConfigFiles();
		txtUsername.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> observable,
					String oldValue,
					String newValue) {
				boolean disableRootPath = false;
				String newValueLower = newValue.toLowerCase();
				if(availableConfigFiles.containsKey(newValueLower)) {

					UserConfig cfg = availableConfigFiles.get(newValueLower);
					logger.info("Found config file for user: '{}' - Path: '{}'",
							newValueLower, cfg.getConfigFile());

					if(cfg.hasRootPath()) {
						txtRootPath.setText(cfg.getRootPath().toString());
						disableRootPath = true;
					}
				}

				// if user can select root path, we propose '.../userhome/synqbox/username' as name
				if(!disableRootPath) {
					Path newUserRootPath = Paths.get(FileUtils.getUserDirectoryPath(),
							Constants.APP_NAME, getUsername());
					txtRootPath.setText(newUserRootPath.toString());
				}

				disableRootPathProperty.set(disableRootPath);
			}
		});

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

	@FXML
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
			final String password = getPassword();
			final String pin = getPin();
			final Path path = getRootPath();

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

		UserConfig userConfig = UserConfigUtils.createUserConfig(getUsername());
		try {
			userConfig.load();
			saveLoginConfig(userConfig);

			initializeServices(userConfig);

		} catch (IOException e) {
			logger.warn("Could not load and save user config.", e);
			setError("Could not load and save user config.");
		}

		resetForm();
	    fNavigationService.clearPages();
		hideWindow();
	}

	private void initializeServices(UserConfig userConfig) {
		try {

			ClientContext ctx = clientContextFactory.create(userConfig);
			appContext.setCurrentClientContext(ctx);

			ctx.getActionExecutor().start();
			ctx.getFolderWatchService().start(userConfig.getRootPath());

			ForceSync forceSync = new ForceSync(ctx);
			forceSync.forceSync(userConfig.getRootPath());

			ctx.getRemoteProfilePersister().start();

		} catch (Exception e) {
			logger.warn("Exception: ", e);
		}
	}

	private void saveLoginConfig(UserConfig userConfig) {
		try {
			userConfig.setUsername(getUsername());
			userConfig.setRootPath(getRootPath());

			// auto login is not enabled at the moment
//			if (chbAutoLogin.isSelected()) {
//				userConfig.setPassword(getPassword());
//				userConfig.setPin(getPin());
//				userConfig.setAutoLogin(true);
//			} else {
//				userConfig.setPassword("");
//				userConfig.setPin("");
//				userConfig.setAutoLogin(false);
//			}
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

	@FXML
	public void registerAction(ActionEvent event) {
		logger.debug("Navigate to register view.");
		fNavigationService.navigate(ViewNames.REGISTER_VIEW);
	}

	@FXML
	public void changeRootPathAction(ActionEvent event) {
		String path = getRootPathAsString();
		Window toOpenDialog = grdForm.getScene().getWindow();
		path = SelectRootPathUtils.showDirectoryChooser(path, toOpenDialog);
		txtRootPath.setText(path);
	}

	@FXML
	public void navigateBackAction(ActionEvent event) {
		logger.debug("Navigate back.");
		fNavigationService.navigateBack();
	}

	private void hideWindow() {
		Runnable close = new Runnable() {
			@Override
			public void run() {
				// not not quit application when stage closes
				Platform.setImplicitExit(false);
				// hide stage
				Stage stage = (Stage) grdForm.getScene().getWindow();
				stage.close();
			}
		};

		if (Platform.isFxApplicationThread()) {
			close.run();
		} else {
			Platform.runLater(close);
		}
	}

	private String getUsername() {
		return txtUsername.getText().trim();
	}

	private String getPassword() {
		return txtPassword.getText();
	}

	private String getPin() {
		return txtPin.getText();
	}

	private Path getRootPath() {
		return Paths.get(getRootPathAsString());
	}

	private String getRootPathAsString() {
		return txtRootPath.getText();
	}

	private void setError(String error) {
		lblError.setText(error);
	}

	private void clearError() {
		lblError.setText("");
	}

}

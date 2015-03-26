package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.peerbox.ResultStatus;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.presenter.validation.CombinedPasswordValidator;
import org.peerbox.presenter.validation.CombinedPinValidator;
import org.peerbox.presenter.validation.UsernameValidator;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;
import org.peerbox.view.ViewNames;
import org.peerbox.view.controls.ErrorLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Controller for user registration form.
 * @author albrecht
 *
 */
public class RegisterController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
	private NavigationService fNavigationService;
	private IUserManager fUserManager;

	@FXML
	private TextField txtUsername;
	@FXML
	private Label lblUsernameError;
	@FXML
	private PasswordField txtPassword_1;
	@FXML
	private Label lblPasswordError;
	@FXML
	private PasswordField txtPassword_2;
	@FXML
	private PasswordField txtPin_1;
	@FXML
	private Label lblPinError;
	@FXML
	private PasswordField txtPin_2;
	@FXML
	private Button btnRegister;
	@FXML
	private Button btnBack;
	@FXML
	private GridPane grdForm;
	@FXML
	private ProgressIndicator piProgress;

	@FXML
	private ErrorLabel lblError;

	private UsernameValidator usernameValidator;
	private CombinedPasswordValidator passwordValidator;
	private CombinedPinValidator pinValidator;

	@Inject
	public RegisterController(NavigationService navigationService, IUserManager userManager) {
		fNavigationService = navigationService;
		fUserManager = userManager;
	}

	public void initialize(URL location, ResourceBundle resources) {
		initializeValidations();
	}

	/**
	 * Resets the controller to an initial state
	 */
	private void resetForm() {
		txtUsername.clear();
		txtPassword_1.clear();
		txtPassword_2.clear();
		txtPin_1.clear();
		txtPin_2.clear();
		grdForm.disableProperty().unbind();
		grdForm.setDisable(false);
		uninstallProgressIndicator();
		uninstallValidationDecorations();
	}

	/**
	 * Installs decorators for field validation
	 */
	private void initializeValidations() {
		usernameValidator = new UsernameValidator(txtUsername, lblUsernameError.textProperty(), fUserManager);
		passwordValidator = new CombinedPasswordValidator(txtPassword_1, lblPasswordError.textProperty(), txtPassword_2);
		pinValidator = new CombinedPinValidator(txtPin_1, lblPinError.textProperty(), txtPin_2);
	}


	/**
	 * Remove the decorations that are installed during validation
	 */
	private void uninstallValidationDecorations() {
		usernameValidator.reset();
		passwordValidator.reset();
		pinValidator.reset();
	}

	/**
	 * Register new user action. Validates user input (on demand) first
	 * @param event that was fired.
	 */
	@FXML
	public void registerAction(ActionEvent event) {
		clearError();
		if (!validateAll().isError()) {
			Task<ResultStatus> task = createRegisterTask();
			new Thread(task).start();
		}
	}

	/**
	 * Complete validation of all input fields AND'ed
	 * @return ANDed validation result
	 */
	private ValidationResult validateAll() {
		// note, we want to evaluate ALL fields, regardless whether one validation fails or not.
		// this way, all fields will be analyzed and marked if validation fails and not just the first
		// field where validation fails.
		// thus: use & and not &&
		return (usernameValidator.validate(true) == ValidationResult.OK
				& passwordValidator.validate() == ValidationResult.OK
				& pinValidator.validate() == ValidationResult.OK)
				? ValidationResult.OK : ValidationResult.ERROR;
	}

	/**
	 * Go back to previous page
	 * @param event that was fired.
	 */
	@FXML
	public void navigateBackAction(ActionEvent event) {
		logger.debug("Navigate back.");
		fNavigationService.navigateBack();
	}

	/**
	 * Navigate to the login page.
	 * @param event that was fired.
	 */
	@FXML
	public void loginAction(ActionEvent event) {
		logger.debug("Navigate to Login page.");
		fNavigationService.navigate(ViewNames.LOGIN_VIEW);
	}

	/**
	 * Registers a new user given the credentials.
	 * All checks regarding the input should happen before (e.g. is registered, password not empty, ...)
	 * @param username the user id
	 * @param password the password
	 * @param pin the pin
	 * @return status of the operation
	 */
	public ResultStatus registerUser(final String username, final String password, final String pin) {
		try {
			return fUserManager.registerUser(username, password, pin);
		} catch (NoPeerConnectionException e) {
			return ResultStatus.error("Could not register user because connection to network failed.");
		}
	}

	/**
	 * Creates an asynchronous task that registers an user.
	 *
	 * @return result of operation
	 */
	private Task<ResultStatus> createRegisterTask() {
		Task<ResultStatus> task = new Task<ResultStatus>() {
			// credentials
			final String username = getUsername();
			final String password = txtPassword_1.getText();
			final String pin = txtPin_1.getText();
			@Override
			public ResultStatus call() {
				return registerUser(username, password, pin);
			}
		};

		task.setOnScheduled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				logger.info("Start registering new user profile");
				installProgressIndicator();
				grdForm.disableProperty().bind(task.runningProperty());
			}
		});

		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				onRegisterFailed(ResultStatus.error("Could not register user."));
			}
		});

		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				ResultStatus result = task.getValue();
				if(result.isOk()) {
					onRegisterSucceeded();
				} else {
					onRegisterFailed(result);
				}
			}
		});

		return task;
	}

	/**
	 * Callback for the async register task
	 */
	private void onRegisterSucceeded() {
		logger.info("Registration task succeeded: user {} registered.", getUsername());
		resetForm();
		fNavigationService.navigate(ViewNames.LOGIN_VIEW);
	}

	/**
	 * Callback for the async register task
	 * @param result of the task
	 */
	private void onRegisterFailed(ResultStatus result) {
		logger.error("Registration task failed: {}", result.getErrorMessage());
		Platform.runLater(() -> {
			uninstallProgressIndicator();
			grdForm.disableProperty().unbind();
			grdForm.requestLayout();
			setError(result.getErrorMessage());
		});
	}

	/**
	 * Shows a progress indicator
	 */
	private void installProgressIndicator() {
		Platform.runLater(() -> {
			// center indicator with respect to the grid
			double xOffset = piProgress.getWidth() / 2.0;
			double yOffset = piProgress.getHeight() / 2.0;
			double x = grdForm.getWidth() / 2.0 - xOffset;
			double y = grdForm.getHeight() / 2.0 - yOffset;
			piProgress.relocate(x, y);
			// show PI
			piProgress.setVisible(true);
		});
	}

	/**
	 * Hides the progress indicator
	 */
	private void uninstallProgressIndicator() {
		Platform.runLater(() -> {
			piProgress.setVisible(false);
		});
	}

	/**
	 * Set an error text
	 * @param error message
	 */
	private void setError(String error) {
		lblError.setText(error);
	}

	/**
	 * Reset the error text
	 */
	private void clearError() {
		lblError.setText("");
	}

	/**
	 * Trimmed username of the text field
	 * @return username
	 */
	private String getUsername() {
		return txtUsername.getText().trim();
	}

}

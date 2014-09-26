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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import jidefx.scene.control.decoration.DecorationUtils;
import jidefx.scene.control.decoration.Decorator;
import jidefx.scene.control.validation.ValidationEvent;
import jidefx.scene.control.validation.ValidationMode;
import jidefx.scene.control.validation.ValidationObject;
import jidefx.scene.control.validation.ValidationUtils;
import jidefx.scene.control.validation.Validator;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.Constants;
import org.peerbox.ResultStatus;
import org.peerbox.model.UserManager;
import org.peerbox.utils.FormValidationUtils;
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
	private UserManager fUserManager;
	
	@FXML
	private TextField txtUsername;
	@FXML
	private PasswordField txtPassword_1;
	@FXML
	private PasswordField txtPassword_2;
	@FXML
	private PasswordField txtPin_1;
	@FXML
	private PasswordField txtPin_2;
	@FXML
	private Button btnRegister;
	@FXML
	private Button btnBack;
	@FXML
	private GridPane grdForm;
	
	@FXML 
	private ErrorLabel lblError;
	
	/* decorator that decorates the UI with a progress indicator during busy tasks */
	private Decorator<ProgressIndicator> fProgressDecoration = null;

	@Inject
	public RegisterController(NavigationService navigationService, UserManager userManager) {
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
		ValidationUtils.validateOnDemand(grdForm);
	}

	/**
	 * Installs decorators for field validation
	 */
	private void initializeValidations() {
		wrapDecorationPane();
		addUsernameValidation();
		addPasswordValidation();
		addPinValidation();
	}

	/**
	 * For field validation, a decoration pane is required that installs the UI elements on 
	 * the form fields. All fields within the decoration pane are covered automatically.
	 */
	private void wrapDecorationPane() {
		Pane dp = FormValidationUtils.wrapInDecorationPane((Pane)grdForm.getParent(), grdForm);
		AnchorPane.setLeftAnchor(dp, 0.0);
		AnchorPane.setTopAnchor(dp, 0.0);
		AnchorPane.setRightAnchor(dp, 0.0);
		
	}

	private void addUsernameValidation() {
		Validator usernameEmptyValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				final String username = getUsername();
				if (username.isEmpty()) {
					return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Please enter a username.");
				}
				return ValidationEvent.OK;
			}
		};
		Validator usernameFullValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				try {
					final String username = getUsername();
					ValidationEvent usernameEmpty = usernameEmptyValidator.call(param);
					if(usernameEmpty != ValidationEvent.OK) {
						return usernameEmpty; 
					}
					if (fUserManager.isRegistered(username)) {
						setError("This username is already taken.");
						return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Username already taken.");
					}
				} catch (NoPeerConnectionException e) {
					setError("Network connection failed.");
					return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Network connection failed.");
				}
				return ValidationEvent.OK;
			}
		};
		
		ValidationUtils.install(txtUsername, usernameEmptyValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtUsername, usernameFullValidator, ValidationMode.ON_DEMAND);
	}

	private void addPasswordValidation() {
		Validator passwordValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				final String password = txtPassword_1.getText();
				ValidationUtils.validateOnDemand(txtPassword_2); // refresh validation result of confirm field
				// as well
				if (password.isEmpty()) {
					return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Please enter a password.");
				}
				if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
					return new ValidationEvent(ValidationEvent.VALIDATION_WARNING, 0, String.format(
							"The password should be at least %d characters long.", Constants.MIN_PASSWORD_LENGTH));
				}
				return ValidationEvent.OK;
			}
		};
		ValidationUtils.install(txtPassword_1, passwordValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtPassword_1, passwordValidator, ValidationMode.ON_DEMAND);
	
		Validator passwordMatchValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				final String password_1 = txtPassword_1.getText();
				final String password_2 = txtPassword_2.getText();
				if (!password_1.equals(password_2)) {
					return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "The passwords are not the same.");
				}
				if (password_2.isEmpty()) {
					return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Please enter a password.");
				}
				return ValidationEvent.OK;
			}
		};
		ValidationUtils.install(txtPassword_2, passwordMatchValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtPassword_2, passwordMatchValidator, ValidationMode.ON_DEMAND);
	}

	private void addPinValidation() {
		Validator pinValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				final String pin = txtPin_1.getText();
				ValidationUtils.validateOnDemand(txtPin_2); // refresh validation result of confirm field
				if (pin.isEmpty()) {
					return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Please enter a pin.");
				}
				if (pin.length() < Constants.MIN_PIN_LENGTH) {
					return new ValidationEvent(ValidationEvent.VALIDATION_WARNING, 0, String.format(
							"The PIN should be at least %d characters long.", Constants.MIN_PIN_LENGTH));
				}
				return ValidationEvent.OK;
			}
		};
		ValidationUtils.install(txtPin_1, pinValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtPin_1, pinValidator, ValidationMode.ON_DEMAND);

		Validator pinMatchValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				final String pin_1 = txtPin_1.getText();
				final String pin_2 = txtPin_2.getText();
				if (!pin_1.equals(pin_2)) {
					return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "The PINs are not the same.");
				}
				if (pin_2.isEmpty()) {
					return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 1, "Please enter a PIN.");
				}
				return ValidationEvent.OK;
			}
		};
		ValidationUtils.install(txtPin_2, pinMatchValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtPin_2, pinMatchValidator, ValidationMode.ON_DEMAND);
	}

	/**
	 * Register new user action. Validates user input (on demand) first
	 * @param event
	 */
	public void registerAction(ActionEvent event) {
		clearError();
		if (ValidationUtils.validateOnDemand(grdForm)) {
			Task<ResultStatus> task = createRegisterTask();
			new Thread(task).start();
		}
	}
	
	/**
	 * Go back to previous page
	 * @param event
	 */
	public void navigateBackAction(ActionEvent event) {
		logger.debug("Navigate back.");
		fNavigationService.navigateBack();
	}

	/**
	 * Registers a new user given the credentials. 
	 * All checks regarding the input should happen before (e.g. is registered, password not empty, ...)
	 * @param username
	 * @param password
	 * @param pin
	 * @return status of the operation
	 */
	public ResultStatus registerUser(final String username, final String password, final String pin) {
		try {
			return fUserManager.registerUser(username, password, pin);
		} catch (NoPeerConnectionException e) {
			return ResultStatus.error("Could not register user because connection to network failed.");
		} catch (InvalidProcessStateException | InterruptedException e) {
			e.printStackTrace();
		}
		return ResultStatus.error("Could not register user.");
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
		fNavigationService.navigate(ViewNames.SELECT_ROOT_PATH_VIEW);
	}

	/**
	 * Callback for the async register task
	 * @param result
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
	 * Adds a progress indicator to the UI
	 */
	private void installProgressIndicator() {
		ProgressIndicator piProgress = new ProgressIndicator();
		fProgressDecoration = new Decorator<>(piProgress, Pos.CENTER);
		DecorationUtils.install(grdForm, fProgressDecoration);
	}

	/**
	 * Removes the progress indicator from the UI
	 */
	private void uninstallProgressIndicator() {
		if(fProgressDecoration != null) {
			DecorationUtils.uninstall(grdForm, fProgressDecoration);
			fProgressDecoration = null;
		}
	}
	
	/**
	 * Set an error text
	 * @param error
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
	 * Trimmed username
	 * @return
	 */
	private String getUsername() {
		return txtUsername.getText().trim();
	}
}

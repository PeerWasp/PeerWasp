package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.StyleClassDecoration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.ResultStatus;
import org.peerbox.model.UserManager;
import org.peerbox.presenter.validation.ValidationUtils;
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
	private ProgressIndicator piProgress;
	
	@FXML 
	private ErrorLabel lblError;
	
	private Decoration errorDecorator;
	
	private PasswordValidator passwordValidator;
	private UsernameValidator usernameValidator;
	private PinValidator pinValidator;


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
		uninstallValidationDecorations();
	}

	/**
	 * Installs decorators for field validation
	 */
	private void initializeValidations() {
		errorDecorator = new StyleClassDecoration("validation-error");
		usernameValidator = new UsernameValidator(txtUsername);
		passwordValidator = new PasswordValidator(txtPassword_1, txtPassword_2);
		pinValidator = new PinValidator(txtPin_1, txtPin_2);
	}
	

	/**
	 * Remove the decorations that are installed during validation
	 */
	private void uninstallValidationDecorations() {
		Decorator.removeDecoration(txtUsername, errorDecorator);
		Decorator.removeDecoration(txtPassword_1, errorDecorator);
		Decorator.removeDecoration(txtPassword_2, errorDecorator);
		Decorator.removeDecoration(txtPin_1, errorDecorator);
		Decorator.removeDecoration(txtPin_2, errorDecorator);
	}

	/**
	 * Register new user action. Validates user input (on demand) first
	 * @param event
	 */
	public void registerAction(ActionEvent event) {
		clearError();
		if (!validateAll().isError()) {
			Task<ResultStatus> task = createRegisterTask();
			new Thread(task).start();
		}
	}
	
	/**
	 * Complete validation of all input fields AND'ed
	 * @return
	 */
	private ValidationResult validateAll() {
		// note, we want to evaluate ALL fields, regardless whether one validation fails or not.
		// this way, all fields will be analyzed and marked if validation fails and not just the first 
		// field where validation fails.
		// thus: use & and not &&
		return (usernameValidator.validateUsername(true) == ValidationResult.OK
				& passwordValidator.validatePassword() == ValidationResult.OK
				& passwordValidator.validateConfirmPassword() == ValidationResult.OK
				& pinValidator.validatePin() == ValidationResult.OK 
				& pinValidator.validateConfirmPin() == ValidationResult.OK) 
				? ValidationResult.OK : ValidationResult.ERROR;
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
	
	
	private final class UsernameValidator {

		private TextField txtUsername;

		public UsernameValidator(TextField txtUsername) {
			this.txtUsername = txtUsername;
			initChangeListener();
		}

		private void initChangeListener() {
			txtUsername.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue,
						String newValue) {
					validateUsername(newValue, false);
				}
			});
		}
		
		public ValidationResult validateUsername(boolean checkIfRegistered) {
			return validateUsername(txtUsername.getText(), checkIfRegistered);
		}

		public ValidationResult validateUsername(String username, boolean checkIfRegistered) {
			try {

				final String usernameTr = username.trim();
				ValidationResult res = ValidationUtils.validateUsername(usernameTr,
						checkIfRegistered, fUserManager);

				if (res.isError()) {
					Decorator.addDecoration(txtUsername, errorDecorator);
				} else {
					Decorator.removeDecoration(txtUsername, errorDecorator);
				}

				return res;

			} catch (NoPeerConnectionException e) {
				setError("Network connection failed.");
			}

			return ValidationResult.ERROR;
		}
	}
	
	private final class PasswordValidator {

		private PasswordField txtPassword;
		private PasswordField txtConfirmPassword;

		public PasswordValidator(PasswordField txtPassword, PasswordField txtConfirmPassword) {
			this.txtPassword = txtPassword;
			this.txtConfirmPassword = txtConfirmPassword;
			initChangeListeners();
		}

		private void initChangeListeners() {
			txtPassword.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue,
						String newValue) {
					validatePassword(newValue);
				}
			});

			txtConfirmPassword.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue,
						String newValue) {
					final String password = txtPassword.getText();
					validateConfirmPassword(password, newValue);
				}
			});
		}
		
		public ValidationResult validatePassword() {
			return validatePassword(txtPassword.getText());
		}

		public ValidationResult validatePassword(final String password) {
			final String confirmPassword = txtConfirmPassword.getText();
			validateConfirmPassword(password, confirmPassword);

			ValidationResult res = ValidationUtils.validatePassword(password);
			if (res.isError()) {
				Decorator.addDecoration(txtPassword, errorDecorator);
			} else {
				Decorator.removeDecoration(txtPassword, errorDecorator);
			}

			return res;
		}
		
		public ValidationResult validateConfirmPassword() {
			return validateConfirmPassword(txtPassword.getText(), txtConfirmPassword.getText());
		}

		public ValidationResult validateConfirmPassword(final String password,
				final String confirmPassword) {
			ValidationResult res = ValidationUtils.validateConfirmPassword(password,
					confirmPassword);
			if (res.isError()) {
				Decorator.addDecoration(txtConfirmPassword, errorDecorator);
			} else {
				Decorator.removeDecoration(txtConfirmPassword, errorDecorator);
			}

			return res;
		}
	}
	
	private final class PinValidator {

		private PasswordField txtPin;
		private PasswordField txtConfirmPin;

		public PinValidator(PasswordField txtPin, PasswordField txtConfirmPin) {
			this.txtPin = txtPin;
			this.txtConfirmPin = txtConfirmPin;
			initChangeListeners();
		}

		private void initChangeListeners() {
			txtPin.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue,
						String newValue) {
					validatePin(newValue);
				}
			});

			txtConfirmPin.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue,
						String newValue) {
					final String pin = txtPin.getText();
					validateConfirmPin(pin, newValue);
				}
			});
		}
		
		public ValidationResult validatePin() {
			return validatePin(txtPin.getText());
		}

		public ValidationResult validatePin(final String pin) {
			final String confirmPin = txtConfirmPin.getText();
			validateConfirmPin(pin, confirmPin);

			ValidationResult res = ValidationUtils.validatePin(pin);
			if (res.isError()) {
				Decorator.addDecoration(txtPin, errorDecorator);
			} else {
				Decorator.removeDecoration(txtPin, errorDecorator);
			}

			return res;
		}
		
		public ValidationResult validateConfirmPin() {
			return validateConfirmPin(txtPin.getText(), txtConfirmPin.getText());
		}

		public ValidationResult validateConfirmPin(final String pin, final String confirmPin) {
			ValidationResult res = ValidationUtils.validateConfirmPin(pin, confirmPin);
			if (res.isError()) {
				Decorator.addDecoration(txtConfirmPin, errorDecorator);
			} else {
				Decorator.removeDecoration(txtConfirmPin, errorDecorator);
			}

			return res;
		}
	}
}

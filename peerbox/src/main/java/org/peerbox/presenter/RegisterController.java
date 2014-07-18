package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

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
import javafx.scene.layout.GridPane;
import jidefx.scene.control.decoration.DecorationUtils;
import jidefx.scene.control.decoration.Decorator;
import jidefx.scene.control.validation.ValidationEvent;
import jidefx.scene.control.validation.ValidationMode;
import jidefx.scene.control.validation.ValidationObject;
import jidefx.scene.control.validation.ValidationUtils;
import jidefx.scene.control.validation.Validator;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.peerbox.RegisterValidation;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

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
	private ProgressIndicator piProgress;
	private Decorator<ProgressIndicator> fProgressDecoration;

	@FXML
	private GridPane grdForm;

	public void initialize(URL location, ResourceBundle resources) {
		fProgressDecoration = new Decorator<>(piProgress, Pos.CENTER);
		initializeValidations();
	}

	private void initializeValidations() {
		addUsernameValidation();
		addPasswordValidation();
		addPinValidation();
	}

	private void resetForm() {
		txtUsername.clear();
		txtPassword_1.clear();
		txtPassword_2.clear();
		txtPin_1.clear();
		txtPin_2.clear();
		grdForm.setDisable(false);
		DecorationUtils.uninstall(grdForm);
	}

	private void addUsernameValidation() {
		Validator usernameValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				try {
					String username = txtUsername.getText().trim();
					if (username.isEmpty()) {
						return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Please enter a username.");
					}
					if (!RegisterValidation.checkUsername(username)) {
						return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Username already taken.");
					}
				} catch (NoPeerConnectionException e) {
					return ValidationEvent.OK;
				}
				return ValidationEvent.OK;
			}

		};
		// ValidationUtils.install(txtUsername, usernameValidator, ValidationMode.ON_FOCUS_LOST); // network
		// too costly?
		ValidationUtils.install(txtUsername, usernameValidator, ValidationMode.ON_DEMAND);
	}

	private void addPasswordValidation() {
		Validator pinValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				String pin = txtPin_1.getText();
				ValidationUtils.validateOnDemand(txtPin_2);
				if (pin.isEmpty()) {
					return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Please enter a pin.");
				}
				if (pin.length() < 3) {
					return new ValidationEvent(ValidationEvent.VALIDATION_WARNING, 0, String.format(
							"The PIN should be at least %d characters long.", 3));
				}
				return ValidationEvent.OK;
			}
		};
		ValidationUtils.install(txtPin_1, pinValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtPin_1, pinValidator, ValidationMode.ON_DEMAND);

		Validator pinMatchValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				String pin_1 = txtPin_1.getText();
				String pin_2 = txtPin_2.getText();
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

	private void addPinValidation() {
		Validator passwordValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				String password = txtPassword_1.getText();
				ValidationUtils.validateOnDemand(txtPassword_2); // refresh validation reuslt of confirm field
				// as well
				if (password.isEmpty()) {
					return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Please enter a password.");
				}
				if (password.length() < 6) {
					return new ValidationEvent(ValidationEvent.VALIDATION_WARNING, 0, String.format(
							"The password should be at least %d characters long.", 5));
				}
				return ValidationEvent.OK;
			}
		};
		ValidationUtils.install(txtPassword_1, passwordValidator, ValidationMode.ON_FLY);
		ValidationUtils.install(txtPassword_1, passwordValidator, ValidationMode.ON_DEMAND);

		Validator passwordMatchValidator = new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				String password_1 = txtPassword_1.getText();
				String password_2 = txtPassword_2.getText();
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

	public void registerAction(ActionEvent event) {
		if (ValidationUtils.validateOnDemand(grdForm)) {
			Task<Boolean> task = createRegisterTask();
			grdForm.disableProperty().bind(task.runningProperty());
			DecorationUtils.install(grdForm, fProgressDecoration);
			new Thread(task).start();
		}
	}

	public void goBack(ActionEvent event) {
		logger.debug("Go back.");
		MainNavigator.goBack();
	}

	private boolean registerNewUser() {
		boolean registerSuccess = false;
		try {
			registerSuccess = H2HManager.INSTANCE.registerUser(txtUsername.getText().trim(), txtPassword_1.getText(),
					txtPin_1.getText());
		} catch (NoPeerConnectionException | InterruptedException | InvalidProcessStateException ex) {
			ex.printStackTrace();
			registerSuccess = false;
		}
		return registerSuccess;
	}

	private Task<Boolean> createRegisterTask() {

		Task<Boolean> task = new Task<Boolean>() {
			@Override
			public Boolean call() {
				return registerNewUser();
			}
		};
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				logger.error("Registration task failed.");
				DecorationUtils.uninstall(grdForm, fProgressDecoration);
				grdForm.disableProperty().unbind();
			}

		});
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				logger.debug("Registration task succeeded: user {} registered.", txtUsername.getText().trim());
				DecorationUtils.uninstall(grdForm, fProgressDecoration);
				grdForm.disableProperty().unbind();
				MainNavigator.navigate("/org/peerbox/view/SelectRootPathView.fxml");
			}
		});
		return task;
	}
}

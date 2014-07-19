package org.peerbox.presenter;

import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

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
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import jidefx.scene.control.decoration.DecorationUtils;
import jidefx.scene.control.decoration.Decorator;
import jidefx.scene.control.validation.ValidationMode;
import jidefx.scene.control.validation.ValidationUtils;
import jidefx.scene.control.validation.Validator;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.peerbox.model.H2HManager;
import org.peerbox.model.UserManager;
import org.peerbox.utils.FormValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


public class LoginController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	private H2HManager h2hManager;
	private UserManager userManager;

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
	
	private Decorator<ProgressIndicator> fProgressDecoration = null;
	
	
	@Inject
	public LoginController(H2HManager h2hManager, UserManager userManager) {
		this.h2hManager = h2hManager;
		this.userManager = userManager;
	}
	
	public void initialize(URL location, ResourceBundle resources) {
		initializeValidations();
		
		Path rootPath = h2hManager.getRootPath();
		if(rootPath != null){
			txtRootPath.setText(rootPath.toString());
		} else {
			txtRootPath.setText("rootPath == null, because not read from config file yet!");
		}
		
	}
	
	private void initializeValidations() {
		addUsernameValidation();
		addPasswordValidation();
		addPinValidation();
	}

	private void addUsernameValidation() {
		Validator usernameValidator = FormValidationUtils.createEmptyTextFieldValidator(
				txtUsername, "Please enter a Username.", true);
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
		boolean inputValid = ValidationUtils.validateOnDemand(grdForm)
				&& SelectRootPathUtils.verifyRootPath(h2hManager, txtRootPath.getText());
		
		if (inputValid) {
			Task<Boolean> task = createLoginTask();
			grdForm.disableProperty().bind(task.runningProperty());
			installProgressIndicator();
			new Thread(task).start();
		}
	}
	
	
	private Task<Boolean> createLoginTask() {
		Task<Boolean> task = new Task<Boolean>() {
			@Override
			public Boolean call() throws NoPeerConnectionException, 
				InvalidProcessStateException, InterruptedException {
				return loginUser();
			}
		};
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				onLoginFailed();
			}

		});
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				try {
					if(task.get()) {
						onLoginSucceeded();
					} else {
						onLoginFailed();
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
					onLoginFailed();
				}
			}
		});
		return task;
	}
	
	private void installProgressIndicator() {
		ProgressIndicator piProgress = new ProgressIndicator();
		fProgressDecoration = new Decorator<>(piProgress, Pos.CENTER);
		DecorationUtils.install(grdForm, fProgressDecoration);
	}

	private void uninstallProgressIndicator() {
		if(fProgressDecoration != null) {
			DecorationUtils.uninstall(grdForm, fProgressDecoration);
		}
	}

	private void onLoginFailed() {
		logger.error("Login task failed.");
		uninstallProgressIndicator();
		grdForm.disableProperty().unbind();
	}
	
	private void onLoginSucceeded() {
		logger.debug("Login task succeeded: user {} logged in.", txtUsername.getText().trim());
		uninstallProgressIndicator();
		grdForm.disableProperty().unbind();
		MainNavigator.navigate("/org/peerbox/view/SetupCompleted.fxml");
	}
	
	private boolean loginUser() throws NoPeerConnectionException, InvalidProcessStateException, InterruptedException {
		return userManager.loginUser(txtUsername.getText().trim(), txtPassword.getText(), txtPin.getText(), h2hManager.getRootPath());
	}
	
	public void registerAction(ActionEvent event) {
		logger.debug("Navigate to register view.");
		MainNavigator.navigate("/org/peerbox/view/RegisterView.fxml");
	}
	
	public void goBack(ActionEvent event){
		logger.debug("Go back.");
		MainNavigator.goBack();
	}
	
	public void btnChangeDirectoryHandler(ActionEvent event){
		String path = txtRootPath.getText();
		Window toOpenDialog = btnLogin.getScene().getWindow();
		path = SelectRootPathUtils.showDirectoryChooser(path, toOpenDialog);
		txtRootPath.setText(path);
	}
	
}

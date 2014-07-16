package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.Connection;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.peerbox.RegisterValidation;
import org.peerbox.model.H2HManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
	private CheckBox chbAutoLogin;
	
	@FXML
	private Button btnRegister;
	
	@FXML
	private Button btnBack;
	
	@FXML
	private Label lblError;
	private final StringProperty errorMessage = new SimpleStringProperty();
	
	
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		errorMessage.bind(lblError.textProperty());
	}
	
	public void registerAction(ActionEvent event) {		
		
		boolean inputValid = validateUserCredentials();
		logger.debug("User input is: " + (inputValid == true ? "valid" : "invalid"));
		boolean autoLogin = chbAutoLogin.isSelected();
		
		if(inputValid) {
			boolean registerSuccess = registerNewUser();;
			System.out.println("Register success: " + registerSuccess);
			if(registerSuccess) {
				if(autoLogin) {
					// TODO: login automatically
				} else {
					MainNavigator.navigate("/org/peerbox/view/LoginView.fxml");
				}
			} else {
				// TODO: show some information
			}
		}
	}
	
	
	private boolean registerNewUser() {
		boolean registerSuccess = false;
		try {
			registerSuccess = H2HManager.INSTANCE.registerUser(txtUsername.getText().trim(), txtPassword_1.getText(), txtPin_1.getText());
		} catch (NoPeerConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			registerSuccess = false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			registerSuccess = false;
		} catch (InvalidProcessStateException e) {
			// TODO Auto-generated catch block
			registerSuccess = false;
			e.printStackTrace();
		}
		return registerSuccess;
	}

	private boolean validateUserCredentials() {
		boolean inputValid = true;	
	
		try {
			if(!RegisterValidation.checkUsername(txtUsername.getText().trim())) {
				// TODO: username is not valid, notify user
				inputValid = false;
			}
		} catch (NoPeerConnectionException e) {
			// TODO: notify user that no peer connection is available
			System.err.println("NoPeerConnection");
		}
		
		if(!RegisterValidation.checkPassword(txtPassword_1.getText(), txtPassword_2.getText())) {
			// TODO: passwords not valid, notify user
			inputValid = false;
		}
		
		if(!RegisterValidation.checkPIN(txtPin_1.getText(), txtPin_2.getText())) {
			// TODO: pins not valid, notify user
			
			inputValid = false;
		}
		
		return inputValid;
	}
	
	public void goBack(ActionEvent event){
		System.out.println("Go back.");	
		MainNavigator.goBack();
	}
}

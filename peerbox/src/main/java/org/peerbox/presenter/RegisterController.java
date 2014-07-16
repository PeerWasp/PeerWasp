package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.peerbox.RegisterValidation;
import org.peerbox.model.H2HManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController implements Initializable {

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
	
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	public void registerAction(ActionEvent event) {
		
		boolean inputValid = validateUserCredentials();
		System.out.println("User input is: " + (inputValid == true ? "valid" : "invalid"));
		boolean autoLogin = chbAutoLogin.isSelected();
		String username = txtUsername.getText().trim();
		String password_1 = txtPassword_1.getText();
		String pin_1 = txtPin_1.getText();

		
		if(inputValid) {
			boolean registerSuccess = false;
			try {
				registerSuccess = H2HManager.INSTANCE.registerUser(username, password_1, pin_1);
			} catch (NoPeerConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				registerSuccess = false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				registerSuccess = false;
			}
			
			System.out.println("Register success: " + registerSuccess);
			if(registerSuccess) {
				System.out.println("Registration success: %s" + autoLogin);
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

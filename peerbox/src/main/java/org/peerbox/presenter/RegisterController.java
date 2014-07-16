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
	private Button btnRegister;
	
	@FXML
	private Button btnBack;
	
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	public void registerAction(ActionEvent event) {
		boolean inputValid = true;
		String username = txtUsername.getText().trim();
		String password_1 = txtPassword_1.getText();
		String password_2 = txtPassword_2.getText();
		String pin_1 = txtPin_1.getText();
		String pin_2 = txtPin_2.getText();
		
		try {
			if(!RegisterValidation.checkUsername(username)) {
				// TODO: username is not valid, notify user
				inputValid = false;
			}
		} catch (NoPeerConnectionException e) {
			// TODO: notify user that no peer connection is available
		}
		
		if(!RegisterValidation.checkPassword(password_1, password_2)) {
			// TODO: passwords not valid, notify user
			inputValid = false;
		}
		
		if(RegisterValidation.checkPIN(pin_1, pin_2)) {
			// TODO: pins not valid, notify user
			
			inputValid = false;
		}
		
		if(inputValid) {
			try {
				H2HManager.INSTANCE.registerUser(username, password_1, pin_1);
			} catch (NoPeerConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}
	
	public void goBack(ActionEvent event){
		System.out.println("Go back.");	
		MainNavigator.goBack();
	}
}

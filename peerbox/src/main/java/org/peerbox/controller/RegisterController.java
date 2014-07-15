package org.peerbox.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.peerbox.RegisterValidation;

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
	private Button goBack;
	
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	public void registerAction(ActionEvent event) {
		RegisterValidation.checkUsername(txtUsername);
		RegisterValidation.checkPassword(txtPassword_1, txtPassword_2);
		RegisterValidation.checkPIN(txtPin_1, txtPin_2);
	}
	
	public void goBack(ActionEvent event){
		System.out.println("Go back.");	
		MainNavigator.goBack();
	}
}

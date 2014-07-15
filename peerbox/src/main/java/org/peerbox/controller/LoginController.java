package org.peerbox.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController implements Initializable {

	@FXML
	private TextField txtUsername;
	
	@FXML
	private PasswordField txtPassword;
	
	@FXML
	private PasswordField txtPin;
	
	@FXML
	private Button btnLogin;
	
	@FXML
	private Button btnRegister;
	
	
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	public void loginAction(ActionEvent event) {
		System.out.println("Login...");
	}
	
	public void registerAction(ActionEvent event) {
		System.out.println("Register...");
		MainNavigator.navigate("../RegisterView.fxml");
	}

}

package org.peerbox.controller;

import java.net.URL;
import java.util.ResourceBundle;

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
	
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	public void registerAction(ActionEvent event) {
		
		if(checkCredentials(txtPassword_1, txtPassword_2)){
			System.out.println("Passwords identical.");
		} else {
			System.out.println("Passwords not identical. Please try again");
		}
		
		if(checkCredentials(txtPassword_1, txtPassword_2)){
			System.out.println("Passwords identical.");
		} else {
			System.out.println("Passwords not identical. Please try again");
		}
		
		
		System.out.println("Register...");
	}
	
	
public void checkUsername(TextField userName){
		//TODO
	}
	
	public boolean checkCredentials(PasswordField input1, PasswordField input2){
		
		boolean check = false;
		
		if (input1.getText().equals(input2.getText())){
			check = true;
		}
		return check;
	}
	
	
}

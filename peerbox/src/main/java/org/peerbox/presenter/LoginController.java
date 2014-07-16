package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@FXML
	private TextField txtUsername;
	
	@FXML
	private PasswordField txtPassword;
	
	@FXML
	private PasswordField txtPin;
	
	@FXML
	private CheckBox chbAutoLogin;
	
	@FXML
	private Button btnLogin;
	
	@FXML
	private Button btnRegister;
	
	private final BooleanProperty formEmpty = new SimpleBooleanProperty(true);
	
	public void initialize(URL location, ResourceBundle resources) {
		
		// bind login button disable to the "emptyness" of the required credentials
		btnLogin.disableProperty().bind(formEmpty);
		formEmpty.bind(txtUsername.textProperty().isEmpty().or(
				txtPassword.textProperty().isEmpty().or(
						txtPin.textProperty().isEmpty())));
	}
	
	public void loginAction(ActionEvent event) {
		
		boolean loginSuccess = true;
		try {
			loginSuccess = H2HManager.INSTANCE.loginUser(txtUsername.getText().trim(), txtPassword.getText(), txtPin.getText());
		} catch (NoPeerConnectionException e) {
			// TODO Auto-generated catch block
			loginSuccess = false;
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			loginSuccess = false;
			e.printStackTrace();
		} catch (InvalidProcessStateException e) {
			// TODO Auto-generated catch block
			loginSuccess = false;
			e.printStackTrace();
		}
		
		if(loginSuccess) {
			logger.info("Login was successful");
		} else {
			logger.warn("Login was not successful");
		}
	}
	
	public void registerAction(ActionEvent event) {
		System.out.println("Register...");
		MainNavigator.navigate("/org/peerbox/view/RegisterView.fxml");
	}
	
	public void goBack(ActionEvent event){
		MainNavigator.goBack();
	}

}

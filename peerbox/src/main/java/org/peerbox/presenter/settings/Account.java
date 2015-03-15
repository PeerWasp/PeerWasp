package org.peerbox.presenter.settings;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.peerbox.app.config.AppConfig;
import org.peerbox.app.config.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class Account implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(Account.class);

	private AppConfig appConfig;
	private UserConfig userConfig;

	@FXML
	private TextField txtUsername;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private PasswordField txtPin;
	@FXML
	private TextField txtRootPath;
	@FXML
	private CheckBox chbTrayNotification;

	@Inject
	public Account(AppConfig appConfig, UserConfig userConfig) {
		this.appConfig = appConfig;
		this.userConfig = userConfig;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		reset();
	}

	private void reset() {
		if(userConfig.hasUsername()) {
			txtUsername.setText(userConfig.getUsername());
		}

		if(userConfig.hasPassword()) {
			txtPassword.setText(userConfig.getPassword());
		}

		if(userConfig.hasPin()) {
			txtPin.setText(userConfig.getPin());
		}

		if(userConfig.hasRootPath()) {
			txtRootPath.setText(userConfig.getRootPath().toString());
		}

		chbTrayNotification.setSelected(appConfig.isTrayNotificationEnabled());
	}

	@FXML
	public void trayNotificationAction(ActionEvent event) {
		boolean enabled = chbTrayNotification.isSelected();
		try {
			appConfig.setTrayNotification(enabled);
		} catch (IOException e) {
			logger.warn("Was not possible to change Tray Notification status.", e);
		}
	}

}

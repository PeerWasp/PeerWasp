package org.peerbox.presenter.settings;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import org.peerbox.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Network settings
 * 
 * @author albrecht
 *
 */
public class Activity implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(Activity.class);

	@FXML
	private TextArea taRecentActivity;
	private UserConfig userConfig;

	public Activity() {
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	@Inject
	public void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
	}
}

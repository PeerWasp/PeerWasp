package org.peerbox.presenter.settings;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import javafx.application.Platform;
import org.peerbox.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	public void appendText(String str) {
	    Platform.runLater(() -> taRecentActivity.appendText(str));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
//	    OutputStream out = new OutputStream() {
//	        @Override
//	        public void write(int b) throws IOException {
//	            appendText(String.valueOf((char) b));
//	        }
//	    };
//	    System.setOut(new PrintStream(out, true));
	}

}

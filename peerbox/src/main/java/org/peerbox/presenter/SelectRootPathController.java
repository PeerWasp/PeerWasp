package org.peerbox.presenter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import org.peerbox.model.H2HManager;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class SelectRootPathController implements Initializable{

	@FXML
	private Button changeDirButton;
	@FXML
	private Button okButton;
	@FXML
	private Button goBackButton;
	@FXML
	private TextField pathTextField;
	
	@FXML
	private Label warningLabel;
	
	
	public void changeDirectory(ActionEvent event){
		DirectoryChooser chooser = new DirectoryChooser();
	    chooser.setTitle("Choose your root directory");
	    chooser.setInitialDirectory(new File(pathTextField.getText()).getParentFile());
	    File selectedDirectory = chooser.showDialog(okButton.getScene().getWindow());
	    if (selectedDirectory != null) {
	        pathTextField.setText(selectedDirectory.getAbsolutePath());
	    }  
	}
	
	public void goBack(ActionEvent event){
		MainNavigator.goBack();
	}
	
	public void okButtonHandler(ActionEvent event){
		try {
			H2HManager.INSTANCE.initializeRootDirectory(pathTextField.getText());
			MainNavigator.navigate("/org/peerbox/view/LoginView.fxml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			warningLabel.setText("This is a file, not a directory. Please provide a directory.");
		}
		
	}


	public void initialize(URL location, ResourceBundle resources) {
		Date now = new Date();
		String defaultDir = System.getProperty("user.home") + File.separator + "PeerBox_" + now.getTime();
		pathTextField.setText(defaultDir);
		pathTextField.setPrefWidth(250);
	}
}

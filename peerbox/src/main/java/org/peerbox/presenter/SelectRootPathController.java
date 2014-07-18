package org.peerbox.presenter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.peerbox.model.H2HManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.peerbox.PropertyHandler;

public class SelectRootPathController implements Initializable{

	@FXML
	private Button btnChangeDirectory;
	@FXML
	private Button btnContinue;
	@FXML
	private Button btnGoBack;
	@FXML
	private TextField txtRootPath;
	@FXML
	private Label lblWarning;
	
	
	public void changeDirectory(ActionEvent event){
		DirectoryChooser chooser = new DirectoryChooser();
	    chooser.setTitle("Choose your root directory");
	    chooser.setInitialDirectory(new File(txtRootPath.getText()).getParentFile());
	    File selectedDirectory = chooser.showDialog(btnContinue.getScene().getWindow());
	    if (selectedDirectory != null) {
	        txtRootPath.setText(selectedDirectory.getAbsolutePath());
	    }  
	}
	
	public void goBack(ActionEvent event){
		MainNavigator.goBack();
	}
	
	public void okButtonHandler(ActionEvent event){
		SelectRootPathUtils.verifyRootPath(txtRootPath.getText());
	}

	public void initialize(URL location, ResourceBundle resources) {
		String defaultDir = null;
		Date now = new Date();

		if(PropertyHandler.rootPathExists() && !PropertyHandler.getRootPath().equals("unset")){
			defaultDir = PropertyHandler.getRootPath();
			H2HManager.INSTANCE.setRootPath(defaultDir);
		} else {
			defaultDir = System.getProperty("user.home") + File.separator + "PeerBox_" + now.getTime();
		}
		txtRootPath.setText(defaultDir);
		txtRootPath.setPrefWidth(250);
	}
}
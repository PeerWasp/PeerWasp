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
		try {
			File path = new File(txtRootPath.getText());
			Action createDirAction = Dialog.Actions.YES;
			boolean isDirCreated = false;
			if(!path.exists()){
				createDirAction = askForDirectoryCreation();
			}
			//TODO rootpath should be read from H2HManager!
			if(createDirAction.equals(Dialog.Actions.YES)){
				isDirCreated = H2HManager.INSTANCE.initializeRootDirectory(txtRootPath.getText());
				if(isDirCreated){
					PropertyHandler.setRootPath(txtRootPath.getText()); //save path in property file
					MainNavigator.navigate("/org/peerbox/view/LoginView.fxml");
				} else {
					showPermissionWarning();
				}
			}
		} catch (IOException e) {
			lblWarning.setText("This is a file, not a directory. Please provide a directory.");
		}
	}

	private void showPermissionWarning() {
		Dialogs.create().title("Directory creation failed")
			.message("Check your permissions")
			.showWarning();
	}

	private Action askForDirectoryCreation() {
		return Dialogs.create()
			      .title("Directory does not exist.")
			      .message( "Create this directory?")
			      .showConfirm();
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
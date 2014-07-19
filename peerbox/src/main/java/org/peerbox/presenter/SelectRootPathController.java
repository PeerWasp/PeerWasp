package org.peerbox.presenter;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import org.peerbox.PropertyHandler;
import org.peerbox.model.H2HManager;

import com.google.inject.Inject;


public class SelectRootPathController implements Initializable{
	
	private H2HManager h2hManager;
	
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
	
	@Inject
	public SelectRootPathController(H2HManager h2hManager) {
		this.h2hManager = h2hManager;
	}
	
	public void changeDirectory(ActionEvent event){ 
		String path = txtRootPath.getText();
		Window toOpenDialog = btnContinue.getScene().getWindow();
		path = SelectRootPathUtils.showDirectoryChooser(path, toOpenDialog);
		txtRootPath.setText(path);
	}
	
	public void goBack(ActionEvent event){
		MainNavigator.goBack();
	}
	
	public void okButtonHandler(ActionEvent event){
		SelectRootPathUtils.verifyRootPath(h2hManager, txtRootPath.getText());
	}

	public void initialize(URL location, ResourceBundle resources) {
		String defaultDir = null;
		Date now = new Date();

		if(PropertyHandler.rootPathExists() && !PropertyHandler.getRootPath().equals("unset")){
			defaultDir = PropertyHandler.getRootPath();
			h2hManager.setRootPath(defaultDir);
		} else {
			defaultDir = System.getProperty("user.home") + File.separator + "PeerBox_" + now.getTime();
		}
		txtRootPath.setText(defaultDir);
		txtRootPath.setPrefWidth(250);
	}
}
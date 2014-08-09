package org.peerbox.presenter;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import org.peerbox.UserConfig;
import org.peerbox.view.ViewNames;

import com.google.inject.Inject;


public class SelectRootPathController implements Initializable{
	
	private NavigationService fNavigationService;
	private UserConfig userConfig; 
	
	@FXML
	private Button btnChangeDirectory;
	@FXML
	private Button btnContinue;
	@FXML
	private Button btnGoBack;
	@FXML
	private TextField txtRootPath;
	
	@Inject
	public SelectRootPathController(NavigationService navigationService) {
		this.fNavigationService = navigationService;
	}
	
	public void changeDirectory(ActionEvent event){ 
		String path = txtRootPath.getText();
		Window toOpenDialog = btnContinue.getScene().getWindow();
		path = SelectRootPathUtils.showDirectoryChooser(path, toOpenDialog);
		txtRootPath.setText(path);
	}
	
	public void goBack(ActionEvent event){
		fNavigationService.navigateBack();
	}
	
	public void okButtonHandler(ActionEvent event){
		boolean inputValid = SelectRootPathUtils.verifyRootPath(userConfig, txtRootPath.getText());
		if(inputValid) {
			fNavigationService.navigate(ViewNames.LOGIN_VIEW);
		}
	}

	public void initialize(URL location, ResourceBundle resources) {
		String defaultDir = null;
		Date now = new Date();

		if(userConfig.rootPathExists()){
			defaultDir = userConfig.getRootPath().toString();
		} else {
			defaultDir = System.getProperty("user.home") + File.separator + "PeerBox_" + now.getTime();
		}
		txtRootPath.setText(defaultDir);
	}
	
	@Inject
	public void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
	}
}
package org.peerbox.presenter;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import org.peerbox.UserConfig;
import org.peerbox.model.H2HManager;
import org.peerbox.view.ViewNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class JoinNetworkController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(JoinNetworkController.class);
	
	private H2HManager h2hManager;
	private NavigationService fNavigationService;
	private UserConfig userConfig; 
	
	@FXML
	private TextField txtBootstrapIP;
	
	@Inject
	public JoinNetworkController(NavigationService navigationService, H2HManager h2hManager) {
		this.fNavigationService = navigationService;
		this.h2hManager = h2hManager;
	}
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		
	}
	
	public void goBack(ActionEvent event){
		System.out.println("Go back.");
		fNavigationService.goBack();
	}
	
	public void accessNetwork(ActionEvent event){
		System.out.println("Try to join network at provided IP address.");
		try {
			if(h2hManager.accessNetwork(txtBootstrapIP.getText().trim())){
				
				udpateUserConfig();
				
				if(!userConfig.rootPathExists()) {
					fNavigationService.navigate(ViewNames.SELECT_ROOT_PATH_VIEW);
				} else {
					fNavigationService.navigate(ViewNames.LOGIN_VIEW);
				}
			}
			// TODO: what is the else case?
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void udpateUserConfig() {
		try {
			userConfig.addBootstrapNode(txtBootstrapIP.getText().trim());
		} catch(IOException ioex) {
			logger.warn("Could not save settings: {}", ioex.getMessage());
			// TODO: inform user.
		}
	}
	
	@Inject
	public void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
	}
}

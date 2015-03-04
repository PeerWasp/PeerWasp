package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import org.peerbox.view.ViewNames;

import com.google.inject.Inject;


public class NetworkSelectionController implements Initializable {

	private NavigationService fNavigationService;

	@Inject
	public NetworkSelectionController(NavigationService navigationService) {
		this.fNavigationService = navigationService;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {

	}

	@FXML
	public void createNetwork(ActionEvent event){
		fNavigationService.navigate(ViewNames.CREATE_NETWORK_VIEW);
	}

	@FXML
	public void joinNetwork(ActionEvent event){
		fNavigationService.navigate(ViewNames.JOIN_NETWORK_VIEW);
	}
}

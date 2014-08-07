package org.peerbox;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.peerbox.interfaces.INavigatable;
import org.peerbox.presenter.NavigationService;
import org.peerbox.view.ViewNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class StartupStage {
	private static final Logger logger = LoggerFactory.getLogger(StartupStage.class);
	private Stage stage;
	private Parent mainView;
	private NavigationService navigationService;

	
	public StartupStage() {
	}
	
	public NavigationService getNavigationService() {
		return navigationService;
	}
	
	@Inject
	public void setNavigationService(NavigationService service) {
		this.navigationService = service;
	}
	
	@Inject
	public void setStage(@Named("PrimaryStage") Stage stage) {
		this.stage = stage;
	}
	
	private void initializeStage() {
		stage.setTitle("PeerBox");
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
		stage.initStyle(StageStyle.DECORATED);
		installExitHandler();
		
		Scene scene = new Scene(mainView, 275, 500);
    	stage.setScene(scene);
    	stage.sizeToScene();
    	
		// Add configuration of stage here
	}
	
	private void installExitHandler() {
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	        @Override
	        public void handle(WindowEvent t) {
	            // TODO: close application properly or hide to tray.
	        	Platform.exit();
	            System.exit(0);
	        }
	
	    });
	}
	
	private void initializeMainView() {   	
		INavigatable mainController;
		try {
			FXMLLoader fxmlLoader = navigationService.createLoader(ViewNames.MAIN_VIEW);
			mainView = fxmlLoader.load();
			mainController = (INavigatable)fxmlLoader.getController();
			
			navigationService.setNavigationController(mainController);
			
		} catch (IOException e) {
			logger.error("Could not load initial view (main view and controller): {}", e.getMessage());
			// TODO handle error properly!
			return;
		}
	}

	public void show() {
		initializeMainView();
    	initializeStage();
    	navigationService.navigate(ViewNames.NETWORK_SELECTION_VIEW);
    	stage.show();
	}
}

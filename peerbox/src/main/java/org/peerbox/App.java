package org.peerbox;


import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.peerbox.PropertyHandler;
import org.peerbox.interfaces.INavigatable;
import org.peerbox.model.H2HManager;
import org.peerbox.presenter.MainController;
import org.peerbox.presenter.NavigationService;
import org.peerbox.view.ViewNames;
import org.peerbox.view.tray.SysTray;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This is the first prototype of graphical user interface.
 */
public class App extends Application
{
	private static final Logger logger = LoggerFactory.getLogger("PeerBox");
	
	private Injector injector;
	private H2HManager h2hManager; 
	
	private Parent mainView;
	private NavigationService navigationService;
	
	public static void main(String[] args) {
		logger.info("PeerBox started.");
		launch(args);
    }
    
    @Override
    public void start(Stage stage) {
    	initializeConfig();
    	
    	initializeGuice();
    	
    	h2hManager = injector.getInstance(H2HManager.class);
    	h2hManager.setRootPath(PropertyHandler.getRootPath());
    	
    	navigationService = injector.getInstance(NavigationService.class);
    	navigationService.setInjector(injector);
    	
    	initializeMainView();
    	initializeStage(stage);
    	initializeSysTray();
    	
    	
    	
		Scene scene = new Scene(mainView, 275, 500);
    	stage.setScene(scene);
    	stage.sizeToScene();
    	navigationService.navigate(ViewNames.NETWORK_SELECTION_VIEW);
		stage.show();

        
    }

	private void initializeStage(Stage stage) {
		stage.setTitle("PeerBox");
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
		stage.initStyle(StageStyle.DECORATED);
		installExitHandler(stage);
		
		// Add configuration of stage here
	}

	private void initializeMainView() {   	
    	INavigatable mainController;
		try {
			FXMLLoader fxmlLoader = navigationService.createGuiceFxmlLoader(ViewNames.MAIN_VIEW);
			mainView = fxmlLoader.load();
			mainController = (INavigatable)fxmlLoader.getController();
			
			navigationService.setNavigationController(mainController);
			
		} catch (IOException e) {
			logger.error("Could not load initial view (main view and controller): {}", e.getMessage());
			// TODO handle error properly!
			return;
		}
	}

	private void initializeConfig() {
		//check whether a Configuration file already exists and load it (if it doesn't exist, it will be created automatically)
		try {
			PropertyHandler.loadProperties();
		} catch (IOException e) {
			logger.warn("Could not load application properties.");
			e.printStackTrace();
		}
	}

	private void initializeSysTray() {
	    try {
	    	SysTray sysTray = new SysTray();
			sysTray.addToTray();
		} catch (AWTException | IOException ex) {
			logger.warn("Could not initialize systray: {}", ex.getMessage());
		} 
	}

	private void launchSetupWizard() {
		// TODO Auto-generated method stub
		
	}

	private void launchInBackground() {
	}

	private boolean isAppConfigured() {
		// for now, single user assumed
		return PropertyHandler.hasUsername();
	}

	private void initializeGuice() {
		injector = Guice.createInjector(new PeerBoxModule());
	}

	private void installExitHandler(Stage stage) {
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                // TODO: close application properly or hide to tray.
            	Platform.exit();
                System.exit(0);
            }

        });
	}
	
	
}


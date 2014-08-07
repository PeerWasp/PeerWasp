package org.peerbox;


import java.awt.AWTException;
import java.io.IOException;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.peerbox.model.H2HManager;
import org.peerbox.view.tray.SysTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * This is the first prototype of graphical user interface.
 */
public class App extends Application
{
	private static final Logger logger = LoggerFactory.getLogger("PeerBox");

	private Injector injector;
	private H2HManager h2hManager;
	private SysTray sysTray;
	private static Stage primaryStage;
	

	public static void main(String[] args) {
		logger.info("PeerBox started.");
		launch(args);
	}
    
    @Override
    public void start(Stage stage) {
    	primaryStage = stage;    	
    	initializeGuice();
		initializeConfig();
		
		h2hManager.setRootPath(PropertyHandler.getRootPath());

		initializeSysTray();
		
		/*
    	 * Following situations may occur:
    	 * - application starts not for the first time + auto login enabled? -> join and login 
    	 * - Otherwise:
    	 * 		* load the start screen and show the UI
    	 */
	    
		// TODO: if join/login fails -> action required? e.g. launch in foreground? 
		// do nothing but indicate with icon?
		if (isAutoLoginFeasible()) {
			logger.info("Auto login feasible, try to join and login.");
			try {
				launchInBackground();
			} catch (NoPeerConnectionException | InvalidProcessStateException | InterruptedException e) {
				logger.error("Could not join and login network.");
				e.printStackTrace();
			}
		} else {
			logger.info("Loading startup stage (no auto login)");
			launchInForeground();
		}     
    }

	private void initializeGuice() {
		injector = Guice.createInjector(new PeerBoxModule());
		injector.injectMembers(this);
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
	    	sysTray.addToTray();
	    	Platform.setImplicitExit(false);
		} catch (AWTException awtex) {
			logger.warn("Could not initialize systray (tray may not be supported?): {}", awtex.getMessage());
		} catch (IOException ioex) {
			logger.warn("Could not initialize systray (image not found?): {}", ioex.getMessage());
		} 
	}


	private boolean isAutoLoginFeasible() {
		return
				/* credentials stored */
				PropertyHandler.hasUsername() &&
				PropertyHandler.hasPassword() &&
				PropertyHandler.hasPin() && 
				PropertyHandler.rootPathExists() &&
				/* bootstrap nodes */
				PropertyHandler.hasBootstrappingNodes() &&
				/* auto login desired */
				PropertyHandler.isAutoLoginEnabled();		
	}

	private void launchInForeground() {
		StartupStage startup = injector.getInstance(StartupStage.class);
		startup.getNavigationService().setInjector(injector);
		startup.show();
	}

	private void launchInBackground() throws NoPeerConnectionException, InvalidProcessStateException, InterruptedException {
		try {
			h2hManager.joinNetwork(PropertyHandler.getBootstrappingNodes());
			org.peerbox.model.UserManager userManager = injector.getInstance(org.peerbox.model.UserManager.class);
			userManager.loginUser(PropertyHandler.getUsername(), 
					PropertyHandler.getPassword(),
					PropertyHandler.getPin(), h2hManager.getRootPath());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Stage getPrimaryStage() {
		return primaryStage;
	}
	
	@Inject 
	private void setSysTray(SysTray sysTray) {
		this.sysTray = sysTray;
	}
	
	@Inject
	private void setH2HManager(H2HManager manager) {
		this.h2hManager = manager;
	}
	
}


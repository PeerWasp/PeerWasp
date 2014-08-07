package org.peerbox;


import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.peerbox.guice.PeerBoxModule;
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
	private UserConfig userConfig;
	

	public static void main(String[] args) {
		logger.info("PeerBox started.");
		launch(args);
	}
    
    @Override
    public void start(Stage stage) {
    	primaryStage = stage;    	
    	initializeGuice();

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
				userConfig.hasUsername() &&
				userConfig.hasPassword() &&
				userConfig.hasPin() && 
				userConfig.rootPathExists() &&
				/* bootstrap nodes */
				userConfig.hasBootstrappingNodes() &&
				/* auto login desired */
				userConfig.isAutoLoginEnabled();		
	}

	private void launchInForeground() {
		StartupStage startup = injector.getInstance(StartupStage.class);
		startup.show();
	}

	private void launchInBackground() throws NoPeerConnectionException, InvalidProcessStateException, InterruptedException {
		try {
			
			Path r = userConfig.getRootPath();
			boolean rootPathOk = Files.isDirectory(r) && Files.isWritable(r);
			
			
			
			h2hManager.joinNetwork(userConfig.getBootstrappingNodes());
			org.peerbox.model.UserManager userManager = injector.getInstance(org.peerbox.model.UserManager.class);
			userManager.loginUser(userConfig.getUsername(), 
					userConfig.getPassword(),
					userConfig.getPin(), userConfig.getRootPath());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Stage getPrimaryStage() {
		return primaryStage;
	}
	
	@Inject
	private void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
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


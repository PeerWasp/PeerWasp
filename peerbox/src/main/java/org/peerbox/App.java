package org.peerbox;


import java.nio.file.Path;
import java.util.List;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.guice.ApiServerModule;
import org.peerbox.guice.PeerBoxModule;
import org.peerbox.model.H2HManager;
import org.peerbox.model.UserManager;
import org.peerbox.notifications.InformationNotification;
import org.peerbox.presenter.tray.TrayException;
import org.peerbox.presenter.validation.SelectRootPathUtils;
import org.peerbox.view.tray.AbstractSystemTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * This is the first prototype of graphical user interface.
 */
public class App extends Application
{
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private Injector injector;
	private EventBus eventBus;
	private H2HManager h2hManager;
	private AbstractSystemTray systemTray;
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
		
		// TODO: if join/login fails -> action required? e.g. launch in foreground? 
		// do nothing but indicate with icon?
		if (isAutoLoginFeasible()) {
			logger.info("Auto login feasible, try to join and login.");
			launchInBackground();
		} else {
			logger.info("Loading startup stage (no auto login)");
			launchInForeground();
		}
		
		eventBus.post(new InformationNotification("PeerBox started", "Hello..."));
    }

	private void initializeGuice() {
		injector = Guice.createInjector(new PeerBoxModule(), new ApiServerModule());
		injector.injectMembers(this);
	}

	private void initializeSysTray() {
		try {
			systemTray.show();
			systemTray.showDefaultIcon();
		} catch (TrayException e) {
			logger.error("Could not initialize systray");
		} 
	}

	private boolean isAutoLoginFeasible() {
		return
				/* credentials stored */
				userConfig.hasUsername() &&
				userConfig.hasPassword() &&
				userConfig.hasPin() && 
				userConfig.hasRootPath() &&
				SelectRootPathUtils.isValidRootPath(userConfig.getRootPath()) &&		
				/* bootstrap nodes */
				userConfig.hasBootstrappingNodes() &&
				/* auto login desired */
				userConfig.isAutoLoginEnabled();
	}

	private void launchInForeground() {
		StartupStage startup = injector.getInstance(StartupStage.class);
		startup.show();
	}

	private void launchInBackground() {
		Task<ResultStatus> task = createJoinLoginTask();
		new Thread(task).start();
	}
	
	private ResultStatus joinAndLogin(List<String> nodes, 
			String username, String password, String pin, Path path) {
		try {
			
			if (!h2hManager.joinNetwork(nodes)) {
				return ResultStatus.error("Could not join network.");
			}
			UserManager userManager = injector.getInstance(org.peerbox.model.UserManager.class);
			return userManager.loginUser(username, password, pin, path);
			
		} catch (NoPeerConnectionException e) {
			logger.debug("Loggin failed: {}", e);
			return ResultStatus.error("Could not login user because connection to network failed.");
		} catch (InvalidProcessStateException | InterruptedException e) {
			e.printStackTrace();
		}
		return ResultStatus.error("Could not login user.");
	}
	
	private Task<ResultStatus> createJoinLoginTask() {
		
		final List<String> nodes = userConfig.getBootstrappingNodes();
		// credentials
		final String username = userConfig.getUsername();
		final String password = userConfig.getPassword();
		final String pin = userConfig.getPin();
		final Path path = userConfig.getRootPath();
		
		Task<ResultStatus> task = new Task<ResultStatus>() {
			@Override
			public ResultStatus call() {
				return joinAndLogin(nodes, username, password, pin, path);
			}
		};

		task.setOnScheduled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				logger.info("Try to join and login using user configuration.");
				logger.info("{} bootstrapping nodes: ", nodes.size(), nodes);
				logger.info("Username: {}, Path: {}", username, path);
			}
		});

		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				logger.warn("Auto login failed.");
			}
		});

		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				ResultStatus result = task.getValue();
				if(result.isOk()) {
					logger.info("Auto login succeeded.");
				} else {
					logger.warn("Auto login failed: {}", result.getErrorMessage());
				}
			}
		});

		return task;
	}
	
	public static Stage getPrimaryStage() {
		return primaryStage;
	}
	
	@Inject
	private void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
	}
	
	@Inject 
	private void setSystemTray(AbstractSystemTray systemTray) {
		this.systemTray = systemTray;
	}
	
	@Inject
	private void setH2HManager(H2HManager manager) {
		this.h2hManager = manager;
	}
	
	@Inject
	private void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}
}


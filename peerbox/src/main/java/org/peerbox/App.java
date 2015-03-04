package org.peerbox;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.stage.Stage;

import org.peerbox.app.AppContext;
import org.peerbox.app.Constants;
import org.peerbox.events.InformationMessage;
import org.peerbox.events.WarningMessage;
import org.peerbox.guice.ApiServerModule;
import org.peerbox.guice.AppConfigModule;
import org.peerbox.guice.AppModule;
import org.peerbox.notifications.InformationNotification;
import org.peerbox.presenter.tray.TrayException;
import org.peerbox.server.IServer;
import org.peerbox.utils.AppData;
import org.peerbox.view.FontAwesomeOffline;
import org.peerbox.view.tray.AbstractSystemTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This is the first prototype of graphical user interface.
 */
public class App extends Application
{
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	/* location of the logging configuration to load at runtime */
	private static final String LOG_CONFIGURATION = "logback.xml";

	/* commandline argument -- directory for storing application data. */
	private static final String PARAM_APP_DIR = "appdir";

	private Injector injector;
	private Stage primaryStage;

	private AppContext appContext;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
    public void start(Stage stage) {
    	primaryStage = stage;

    	initializeAppFolder();

    	initializeLogging();

    	logger.info("{} starting.", Constants.APP_NAME);
		logger.info("AppData Folder: {}", AppData.getDataFolder());

    	initializeGuice();

    	initializeAppContext();

    	loadAppConfig();

		startServer();

		initializeFonts();

		initializeSysTray();

		// TODO: if join/login fails -> action required? e.g. launch in foreground? do nothing but indicate with icon?
		if (isAutoLoginFeasible()) {
			logger.info("Auto login feasible, try to join and login.");
//			launchInBackground();
		} else {
			logger.info("Loading startup stage (no auto login)");
			launchInForeground();
		}

		appContext.getMessageBus().publish(new InformationMessage("PeerBox started", "Hello..."));
		appContext.getMessageBus().publish(new WarningMessage("PeerBox started", "Hello..."));

		appContext.getMessageBus().publish(new InformationNotification("PeerBox started", "Hello!"));

    }

	private void initializeAppFolder() {
		// check arguments passed via command line
		if (getParameters().getNamed().containsKey(PARAM_APP_DIR)) {
			Path appDir = Paths.get(getParameters().getNamed().get(PARAM_APP_DIR));
			AppData.setDataFolder(appDir);
		}

		boolean success = false;
		try {
			// create folders and check write access
			AppData.createFolders();
			AppData.checkAccess();
			success = true;
		} catch (IOException ioex) {
			logger.warn("Could not initialize application folders: {}.", ioex.getMessage(), ioex);
		}

		if (!success) {
			fatalExit(1);
		}
	}

	private void initializeLogging() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			JoranConfigurator jc = new JoranConfigurator();
			jc.setContext(context);
			context.reset(); // override default configuration
			// inject the location of the appdata log folder as "LOG_FOLDER"
			// property of the LoggerContext
			context.putProperty("LOG_FOLDER", AppData.getLogFolder().toString());
			jc.doConfigure(LOG_CONFIGURATION);
		} catch (JoranException je) {
			// status printer will handle printing of error
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
		logger.debug("Initialized logging (LOG_FOLDER={})", context.getProperty("LOG_FOLDER"));
	}

	private void loadAppConfig() {
		try {
			appContext.getAppConfig().load();
		} catch (IOException ioex) {
			// TODO: we should probably show a message as well
			logger.warn("Could not load app properties: {}",
					appContext.getAppConfig().getConfigFile(), ioex);
		}
	}

	private void initializeGuice() {
		injector = Guice.createInjector(
				new AppModule(primaryStage),
				new AppConfigModule(),
				new ApiServerModule()
		);
	}

	private void initializeAppContext() {
		appContext = injector.getInstance(AppContext.class);
	}

	private void startServer() {
		IServer server = appContext.getServer();
		boolean success = server.start();
		if (success) {
			try {
				appContext.getAppConfig().setApiServerPort(server.getPort());
			} catch (IOException e) {
				logger.warn("Could not set and save server port.", e);
				success = false;
			}
		} else {
			logger.warn("Could not start server.");
		}
	}

	private void initializeFonts() {
		FontAwesomeOffline.init();
	}

	private void initializeSysTray() {
		try {
			AbstractSystemTray systemTray = appContext.getUiContext().getSystemTray();
			systemTray.show();
			systemTray.showDefaultIcon();
		} catch (TrayException e) {
			logger.error("Could not initialize systray");
		}
	}

	private boolean isAutoLoginFeasible() {
		return false;
//				/* credentials stored */
//				userConfig.hasUsername() &&
//				userConfig.hasPassword() &&
//				userConfig.hasPin() &&
//				userConfig.hasRootPath() &&
//				SelectRootPathUtils.isValidRootPath(userConfig.getRootPath()) &&
//				/* bootstrap nodes */
////				userConfig.hasBootstrappingNodes() &&
//				/* auto login desired */
//				userConfig.isAutoLoginEnabled();
	}

	private void launchInForeground() {
		StartupStage startup = injector.getInstance(StartupStage.class);
		startup.show();
	}

//	private void launchInBackground() {
//		Task<ResultStatus> task = createJoinLoginTask();
//		new Thread(task).start();
//	}
//
//	private ResultStatus joinAndLogin(List<String> nodes,
//			String username, String password, String pin, Path path) {
//		try {
//
//			if (!nodeManager.joinNetwork(nodes)) {
//				return ResultStatus.error("Could not join network.");
//			}
//			IUserManager userManager = injector.getInstance(IUserManager.class);
//			return userManager.loginUser(username, password, pin, path);
//
//		} catch (NoPeerConnectionException e) {
//			logger.debug("Loggin failed: {}", e);
//			return ResultStatus.error("Could not login user because connection to network failed.");
//		}
//	}
//
//	private Task<ResultStatus> createJoinLoginTask() {
//
//		final List<String> nodes = null; //userConfig.getBootstrappingNodes();
//		// credentials
//		final String username = userConfig.getUsername();
//		final String password = userConfig.getPassword();
//		final String pin = userConfig.getPin();
//		final Path path = userConfig.getRootPath();
//
//		Task<ResultStatus> task = new Task<ResultStatus>() {
//			@Override
//			public ResultStatus call() {
//				return joinAndLogin(nodes, username, password, pin, path);
//			}
//		};
//
//		task.setOnScheduled(new EventHandler<WorkerStateEvent>() {
//			@Override
//			public void handle(WorkerStateEvent event) {
//				logger.info("Try to join and login using user configuration.");
//				logger.info("{} bootstrapping nodes: ", nodes.size(), nodes);
//				logger.info("Username: {}, Path: {}", username, path);
//			}
//		});
//
//		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
//			@Override
//			public void handle(WorkerStateEvent event) {
//				logger.warn("Auto login failed.");
//			}
//		});
//
//		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//			@Override
//			public void handle(WorkerStateEvent event) {
//				ResultStatus result = task.getValue();
//				if(result.isOk()) {
//					logger.info("Auto login succeeded.");
//				} else {
//					logger.warn("Auto login failed: {}", result.getErrorMessage());
//				}
//			}
//		});
//
//		return task;
//	}

	private void fatalExit(int exitCode) {
		logger.warn("Exiting... (ExitCode: {})", exitCode);
		System.exit(exitCode);
	}
}

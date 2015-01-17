package org.peerbox;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.peerbox.app.Constants;
import org.peerbox.app.activity.collectors.ActivityConfiguration;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.events.MessageBus;
import org.peerbox.guice.ApiServerModule;
import org.peerbox.guice.PeerBoxModule;
import org.peerbox.guice.UserConfigModule;
import org.peerbox.notifications.InformationNotification;
import org.peerbox.presenter.tray.TrayException;
import org.peerbox.presenter.validation.SelectRootPathUtils;
import org.peerbox.server.IServer;
import org.peerbox.utils.AppData;
import org.peerbox.view.tray.AbstractSystemTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.google.inject.Guice;
import com.google.inject.Inject;
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
	private MessageBus messageBus;
	private INodeManager nodeManager;
	private AbstractSystemTray systemTray;
	private Stage primaryStage;
	private UserConfig userConfig;
	private IServer server;

	private ActivityConfiguration activityConfiguration;

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

    	loadConfig();

		startServer();

		initializeSysTray();

		// TODO: if join/login fails -> action required? e.g. launch in foreground? do nothing but indicate with icon?
		if (isAutoLoginFeasible()) {
			logger.info("Auto login feasible, try to join and login.");
			launchInBackground();
		} else {
			logger.info("Loading startup stage (no auto login)");
			launchInForeground();
		}

		messageBus.post(new InformationNotification("PeerBox started", "Hello...")).now();;
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

	private void loadConfig() {
		try {
			userConfig.load();
		} catch (IOException ioex) {
			// TODO: we should probably show a message and exit.
			logger.warn("Could not load user properties: {}", userConfig.getConfigFileName(), ioex);
		}
	}

	private void initializeGuice() {
		injector = Guice.createInjector(
				new PeerBoxModule(primaryStage),
				new UserConfigModule(),
				new ApiServerModule());
		injector.injectMembers(this);
	}

	private void startServer() {
		try {
			boolean success = server.start();
			if(success) {
				userConfig.setApiServerPort(server.getPort());
			} else {
				logger.warn("Could not start server.");
			}
		} catch(IOException e) {
			logger.error("Could not save API server port.", e);
		}
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

			if (!nodeManager.joinNetwork(nodes)) {
				return ResultStatus.error("Could not join network.");
			}
			IUserManager userManager = injector.getInstance(IUserManager.class);
			return userManager.loginUser(username, password, pin, path);

		} catch (NoPeerConnectionException e) {
			logger.debug("Loggin failed: {}", e);
			return ResultStatus.error("Could not login user because connection to network failed.");
		}
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

	private void fatalExit(int exitCode) {
		logger.warn("Exiting... (ExitCode: {})", exitCode);
		System.exit(exitCode);
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
	private void setNodeManager(INodeManager manager) {
		this.nodeManager = manager;
	}

	@Inject
	private void setMessageBus(MessageBus messageBus) {
		this.messageBus = messageBus;
	}

	@Inject
	private void setServer(IServer server) {
		this.server = server;
	}

	@Inject
	private void setActivityConfiguration(ActivityConfiguration activityConfiguration) {
		this.activityConfiguration = activityConfiguration;
	}
}


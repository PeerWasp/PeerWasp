package org.peerbox;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import org.peerbox.app.AppContext;
import org.peerbox.app.ClientContextFactory;
import org.peerbox.app.Constants;
import org.peerbox.app.config.AppConfig;
import org.peerbox.events.InformationMessage;
import org.peerbox.guice.ApiServerModule;
import org.peerbox.guice.AppConfigModule;
import org.peerbox.guice.AppModule;
import org.peerbox.notifications.InformationNotification;
import org.peerbox.presenter.tray.TrayException;
import org.peerbox.server.IServer;
import org.peerbox.utils.DialogUtils;
import org.peerbox.utils.AppData;
import org.peerbox.utils.IconUtils;
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
 * This class is responsible for the initialization of the application.
 * This covers everything until the first window is loaded.
 *
 * @author albrecht
 *
 */
public class App extends Application
{
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	/* location of the logging configuration to load at runtime */
	private static final String LOG_CONFIGURATION = "logback.xml";

	/* commandline argument -- directory for storing application data. */
	private static final String PARAM_APP_DIR = "appdir";

	/* Guice dependency injection - root injector */
	private Injector injector;

	/* primary stage as provided by the JavaFX framework */
	private Stage primaryStage;

	/* application context: application-wide instances and dependencies */
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

		launchInForeground();

		appContext.getMessageBus().publish(new InformationMessage("PeerWasp started", "Hello!"));
		appContext.getMessageBus().publish(new InformationNotification("PeerWasp started", "Hello!"));

    }

	/**
	 * Initializes the folder for application data (appdata folder).
	 * It is possible to specify the folder using a command line parameter.
	 *
	 * Example: --appdir=/home/user/PeerWasp/AppData
	 */
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

	/**
	 * Initializes the logging framework.
	 * The automatic configuration is reset and the logger is configured dynamically:
	 * - The log folder is set
	 * - The log configuration is loaded (not from resources, but from the working directory).
	 *
	 * This allows switching the folder and the configuration during development and at deployment.
	 */
	private void initializeLogging() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			JoranConfigurator jc = new JoranConfigurator();
			jc.setContext(context);
			// override default configuration
			context.reset();
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
		AppConfig appConfig = appContext.getAppConfig();
		try {

			appConfig.load();

		} catch (IOException ioex) {
			logger.warn("Could not load app properties: {}", appConfig.getConfigFile(), ioex);
			// error alert
			String title = "Could not load application properties.";
			String message = String.format("File: %s\nError: %s",
					appConfig.getConfigFile(), ioex.getMessage());
			showErrorAlert(title, message);
		}
	}

	/**
	 * Initialization of Guice dependency injection. This is the root injector, mainly used for
	 * application-wide instances.
	 *
	 * A child injector is created for a specific user during the login procedure
	 * (see {@link ClientContextFactory}.
	 *
	 * Add additional modules here.
	 */
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

	/**
	 * Starts the HTTP server for the context menu.
	 */
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

	/**
	 * Font awesome is an icon font. The font is included in the resources. However, the font
	 * needs to be registered in the glyph font registry of the controlfx library.
	 */
	private void initializeFonts() {
		// just static initialization of the font awesome provider lib
		IconUtils.initFontAwesomeOffline();
	}

	/**
	 * Adds the tray icon to the system tray
	 */
	private void initializeSysTray() {
		try {
			AbstractSystemTray systemTray = appContext.getUiContext().getSystemTray();
			systemTray.show();
			systemTray.showDefaultIcon();
		} catch (TrayException e) {
			logger.error("Could not initialize systray");
		}
	}

	/**
	 * Launches the initial setup window
	 */
	private void launchInForeground() {
		logger.info("Launch startup stage.");
		StartupStage startup = injector.getInstance(StartupStage.class);
		startup.show();
	}

	/**
	 * Force quit application with exit code.
	 *
	 * @param exitCode
	 */
	private void fatalExit(int exitCode) {
		logger.warn("Exiting... (ExitCode: {})", exitCode);
		System.exit(exitCode);
	}

	/**
	 * Shows an alert dialog with an error message.
	 * Blocks until dialog closed.
	 *
	 * @param title
	 * @param message
	 */
	private void showErrorAlert(String title, String message) {
		Alert dlg = DialogUtils.createAlert(AlertType.ERROR);
		dlg.setTitle("Error - Initialization");
		dlg.setHeaderText(title);
		dlg.setContentText(message);
		dlg.showAndWait();
	}
}

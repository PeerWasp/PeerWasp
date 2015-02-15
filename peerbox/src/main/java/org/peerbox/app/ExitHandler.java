package org.peerbox.app;

import javafx.application.Platform;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.AppContext;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.server.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ExitHandler implements IExitHandler {

	private static final Logger logger = LoggerFactory.getLogger(ExitHandler.class);

	private final AppContext appContext;

	@Inject
	public ExitHandler(AppContext appContext) {
		this.appContext = appContext;
	}

	@Override
	public void exit() {
		exit(0);
	}

	private void exit(int status) {
		if (appContext != null) {
			if (appContext.getCurrentClientContext() != null) {
				shutdownClient();
			}

			shutdownApp();
		}

		shutdown(status);
	}

	/**
	 * Shutdown of application-wide services
	 */
	private void shutdownApp() {
		stopServer();
	}

	/**
	 * Shutdown of user-specific services
	 */
	private void shutdownClient() {
		logout();
		disconnect();
	}

	/**
	 * Framework shutdown and final exit
	 * @param status
	 */
	private void shutdown(int status) {
		logger.debug("Shutdown with status code = {}", status);
		// stop JavaFX application thread
		Platform.exit();
		System.exit(status);
	}

	private void stopServer() {
		if (appContext.getServer() != null) {
			IServer server = appContext.getServer();
			boolean success = server.stop();
			if (!success) {
				logger.error("Could not stop API server properly.");
			}
		}
	}

	private void logout() {
		try {
			IUserManager userManager = appContext.getCurrentClientContext().getUserManager();
			if (userManager != null && userManager.isLoggedIn()) {
				userManager.logoutUser();
			}

		} catch (NoPeerConnectionException npc) {
			logger.error("Cannot logout - no peer connection. ", npc);
		} catch (NoSessionException nse) {
			logger.error("Cannot logout - no session. ", nse);
		}
	}

	private void disconnect() {
		INodeManager nodeManager = appContext.getCurrentClientContext().getNodeManager();
		if (nodeManager != null && nodeManager.isConnected()) {
			boolean success = nodeManager.leaveNetwork();
			if (!success) {
				logger.error("Could not disconnect from network properly.");
			}
		}
	}

}

package org.peerbox.app;

import javafx.application.Platform;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.events.MessageBus;
import org.peerbox.server.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * The ExitHandler is responsible for shutting down the application in a controlled way.
 * All services should be stopped and the client should do a graceful leave (logout and
 * disconnect from the network).
 *
 * @author albrecht
 *
 */
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
		try {
			if (appContext != null) {
				ClientContext clientContext = appContext.getCurrentClientContext();
				if (clientContext != null) {
					shutdownClient(clientContext);
				}

				shutdownApp(appContext);
			}
		} catch (Throwable t) {
			logger.warn("Exception occurred during exit handler.", t);
			if (t instanceof Exception) {
				logger.warn("Exception: ", t);
			}
		} finally {
			shutdown(status);
		}
	}

	/**
	 * Shutdown of application-wide services
	 * @param appContext2
	 */
	private void shutdownApp(AppContext context) {
		stopMessageBus(context);
		stopServer(context);
	}

	/**
	 * Shutdown of user-specific services
	 * @param clientContext
	 */
	private void shutdownClient(ClientContext context) {
		logout(context);
		disconnect(context);
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

	/**
	 * Shutdown the message bus
	 * @param context
	 */
	private void stopMessageBus(AppContext context) {
		try {
			MessageBus messageBus = context.getMessageBus();
			if (messageBus != null) {
				messageBus.shutdown();
			}
		} catch (Exception e) {
			logger.warn("Could not shutdown message bus.", e);
		}
	}

	/**
	 * Stop the HTTP server
	 * @param context
	 */
	private void stopServer(AppContext context) {
		try {
			IServer server = context.getServer();
			if (server != null) {
				boolean success = server.stop();
				if (!success) {
					logger.error("Could not stop API server properly.");
				}
			}
		} catch (Exception e) {
			logger.warn("Could not shutdown server.", e);
		}
	}

	/**
	 * Logout H2H user profile
	 * @param context
	 */
	private void logout(ClientContext context) {
		try {
			IUserManager userManager = context.getUserManager();
			if (userManager != null && userManager.isLoggedIn()) {
				userManager.logoutUser();
			}
		} catch (NoPeerConnectionException npc) {
			logger.error("Cannot logout - no peer connection. ", npc);
		} catch (NoSessionException nse) {
			logger.error("Cannot logout - no session. ", nse);
		} catch (Exception e) {
			logger.warn("Could not logout.", e);
		}
	}

	/**
	 * Disconnect from H2H network (graceful leave)
	 * @param context
	 */
	private void disconnect(ClientContext context) {
		try {
			INodeManager nodeManager = context.getNodeManager();
			if (nodeManager != null && nodeManager.isConnected()) {
				boolean success = nodeManager.leaveNetwork();
				if (!success) {
					logger.error("Could not disconnect from network properly.");
				}
			}
		} catch (Exception e) {
			logger.warn("Could not disconnect.", e);
		}
	}

}

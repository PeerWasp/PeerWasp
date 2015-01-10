package org.peerbox.app;

import javafx.application.Platform;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.server.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ExitHandler implements IExitHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(ExitHandler.class);
	
	// TODO(AA): make these references available.
	private IServer server;
	private IUserManager userManager;
	private INodeManager nodeManager;
	
	
	// TODO(AA): shutdown message bus
	
	@Override
	public void exit() {
		exit(0);
	}

	private void exit(int status) {
		stopServer();
		logout();
		disconnect();
		
		Platform.exit(); // stop application thread
		System.exit(status);
	}
	
	private void stopServer() {
		if(server != null) {
			server.stop();
		}
	}
	
	private void logout() {
		try {
			
			if(userManager != null && userManager.isLoggedIn()) {
				userManager.logoutUser();
			}
			
		} catch(NoPeerConnectionException npc) {
			logger.debug("Cannot logout - no peer connection. ", npc);
		} catch(NoSessionException nse) {
			logger.debug("Cannot logout - no session. ", nse);
		}
	}
	
	private void disconnect() {
		if(nodeManager != null && nodeManager.isConnected()) {
			boolean success = nodeManager.leaveNetwork();
			if(!success) {
				logger.debug("Could not disconnect from network properly.");
			}
		}
	}
	
}

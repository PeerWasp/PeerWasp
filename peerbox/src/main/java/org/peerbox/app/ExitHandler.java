package org.peerbox.app;

import javafx.application.Platform;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.IUserManager;
import org.peerbox.model.H2HManager;
import org.peerbox.server.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ExitHandler implements IExitHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(ExitHandler.class);
	
	// TODO(AA): make these references available.
	private IServer server;
	private IUserManager userManager;
	private H2HManager h2hManager;
	
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
		} catch(InvalidProcessStateException | ProcessExecutionException  e) {
			logger.debug("Cannot logout, process exception ", e);
		} 
	}
	
	private void disconnect() {
		if(h2hManager != null && h2hManager.isConnected()) {
			boolean success = h2hManager.leaveNetwork();
			if(!success) {
				logger.debug("Could not disconnect from network properly.");
			}
		}
	}
	
}

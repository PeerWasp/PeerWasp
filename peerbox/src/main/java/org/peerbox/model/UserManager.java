package org.peerbox.model;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.concretes.ProcessComponentListener;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.security.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManager {
	
	private static final Logger logger = LoggerFactory.getLogger(UserManager.class);
	
	private IH2HNode node;
	private UserCredentials userCredentials;
	
	public UserManager(IH2HNode node) {
		this.node = node;
	}
	
	public boolean registerUser(String username, String password, String pin) 
			throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		// do not save the credentials here, wait until login.
		UserCredentials credentials = new UserCredentials(username, password, pin);
		IProcessComponent registerProcess = node.getUserManager().register(credentials);
		ProcessComponentListener listener = new ProcessComponentListener();
		registerProcess.attachListener(listener);
		registerProcess.start().await();
	
		if (listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			System.out.println((String.format("The process has failed: %s", reason != null ? ": " + reason.getHint() : ".")));
		}
	
		return listener.hasSucceeded() && isRegistered(credentials.getUserId());
		
	}
	
	public boolean isRegistered(String userName) throws NoPeerConnectionException{
		return node.getUserManager().isRegistered(userName);
	}

	public boolean loginUser(String username, String password, String pin, Path rootPath) 
			throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		// TODO: what to do with the user credentials? where to get them?
		userCredentials = new UserCredentials(username, password, pin);
		
		System.out.println("Root path " + rootPath);
		
		IProcessComponent process = node.getUserManager().login(userCredentials, rootPath);
		ProcessComponentListener listener = new ProcessComponentListener();
		process.attachListener(listener);
		process.start().await();

		if (listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			System.out.println(String.format("The process has failed%s", reason != null ? ": " + reason.getHint() : "."));
		}
		
		return listener.hasSucceeded() && isLoggedIn(userCredentials.getUserId());
	}
	
	public boolean isLoggedIn(String userName) throws NoPeerConnectionException{
		return node.getUserManager().isLoggedIn(userName);
	}
	
	public boolean logoutUser() 
			throws InvalidProcessStateException, InterruptedException, NoPeerConnectionException, NoSessionException {
		IProcessComponent process = node.getUserManager().logout();
		ProcessComponentListener listener = new ProcessComponentListener();
		process.attachListener(listener);
		process.start().await();
		
		if(listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			logger.error("Logout failed: {}", reason.getHint());
		}
		
		return listener.hasSucceeded();
	}
	
}

package org.peerbox.model;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.concretes.ProcessComponentListener;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.security.UserCredentials;
import org.peerbox.ResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManager {

	private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

	private IUserManager h2hUserManager;
	private UserCredentials userCredentials;

	public UserManager(IUserManager h2hUserManager) {
		this.h2hUserManager = h2hUserManager;
	}

	public ResultStatus registerUser(final String username, final String password, final String pin)
			throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		UserCredentials credentials = new UserCredentials(username, password, pin);
		IProcessComponent registerProcess = h2hUserManager.register(credentials);
		ProcessComponentListener listener = new ProcessComponentListener();
		registerProcess.attachListener(listener);
		registerProcess.start().await();

		if (listener.hasSucceeded() && isRegistered(credentials.getUserId())) {
			return ResultStatus.ok();
		} else if (listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			if (reason != null) {
				return ResultStatus.error(reason.getHint());
			}
		}
		return ResultStatus.error("Could not register user.");
	}
	
	public boolean isRegistered(final String userName) throws NoPeerConnectionException {
		return h2hUserManager.isRegistered(userName);
	}

	public boolean loginUser(String username, String password, String pin, Path rootPath) 
			throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		// TODO: what to do with the user credentials? where to get them?
		userCredentials = new UserCredentials(username, password, pin);
		
		System.out.println("Root path " + rootPath);
		
		IProcessComponent process = h2hUserManager.login(userCredentials, rootPath);
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
		return h2hUserManager.isLoggedIn(userName);
	}
	
	public boolean logoutUser() 
			throws InvalidProcessStateException, InterruptedException, NoPeerConnectionException, NoSessionException {
		IProcessComponent process = h2hUserManager.logout();
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

package org.peerbox.model;

import java.io.IOException;
import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.peerbox.ResultStatus;
import org.peerbox.h2h.PeerboxFileAgent;
import org.peerbox.h2h.ProcessListener;
import org.peerbox.utils.AppData;
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
			throws InvalidProcessStateException, ProcessExecutionException, NoPeerConnectionException {
		UserCredentials credentials = new UserCredentials(username, password, pin);
		IProcessComponent<Void> registerProcess = h2hUserManager.createRegisterProcess(credentials);
		ProcessListener listener = new ProcessListener();
		registerProcess.attachListener(listener);
		registerProcess.execute();

		if (listener.hasExecutionSucceeded() && isRegistered(credentials.getUserId())) {
			return ResultStatus.ok();
		}
		if (listener.hasExecutionFailed()) {
			// TODO: rollback reason?
			return ResultStatus.error("Could not register user.");
		}
		return ResultStatus.error("Could not register user.");
	}

	public boolean isRegistered(final String userName) throws NoPeerConnectionException {
		return h2hUserManager.isRegistered(userName);
	}

	public ResultStatus loginUser(final String username, final String password, final String pin,
			final Path rootPath) throws NoPeerConnectionException,
			InvalidProcessStateException, ProcessExecutionException, IOException {
		// TODO what if already logged in?
		userCredentials = new UserCredentials(username, password, pin);
		IFileAgent fileAgent = new PeerboxFileAgent(rootPath, AppData.getCacheFolder());

		IProcessComponent<Void> loginProcess = h2hUserManager.createLoginProcess(userCredentials, fileAgent);
		ProcessListener listener = new ProcessListener();
		loginProcess.attachListener(listener);
		loginProcess.execute();

		if (listener.hasExecutionSucceeded() && isLoggedIn()) {
			return ResultStatus.ok();
		}
		if (listener.hasExecutionFailed()) {
			// TODO: rollback reason?
			return ResultStatus.error("Could not login user.");
		}
		return ResultStatus.error("Could not login user.");
	}

	public boolean isLoggedIn() throws NoPeerConnectionException {
		return h2hUserManager.isLoggedIn();
	}

	public ResultStatus logoutUser() throws InvalidProcessStateException, ProcessExecutionException, 
			NoPeerConnectionException, NoSessionException {
		IProcessComponent<Void> logoutProcess = h2hUserManager.createLogoutProcess();
		ProcessListener listener = new ProcessListener();
		logoutProcess.attachListener(listener);
		logoutProcess.execute();

		if (listener.hasExecutionSucceeded()) {
			return ResultStatus.ok();
		}
		if (listener.hasExecutionFailed()) {
			// TODO: rollback reason?
			return ResultStatus.error("Could not logout user.");
		}
		return ResultStatus.error("Could not logout user.");
	}
}

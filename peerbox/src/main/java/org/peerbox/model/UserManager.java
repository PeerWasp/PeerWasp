package org.peerbox.model;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.concretes.ProcessComponentListener;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.core.security.UserCredentials;
import org.peerbox.ResultStatus;
import org.peerbox.h2h.PeerboxFileAgent;
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
		}
		if (listener.hasFailed()) {
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

	public ResultStatus loginUser(final String username, final String password, final String pin,
			final Path rootPath) throws NoPeerConnectionException, InterruptedException,
			InvalidProcessStateException {
		// TODO what if already logged in?
		userCredentials = new UserCredentials(username, password, pin);
		
		IProcessComponent loginProcess = h2hUserManager.login(userCredentials, new PeerboxFileAgent(rootPath.toFile()));
		ProcessComponentListener listener = new ProcessComponentListener();
		loginProcess.attachListener(listener);
		loginProcess.start().await();

		if (listener.hasSucceeded() && isLoggedIn(userCredentials.getUserId())) {
			return ResultStatus.ok();
		}
		if (listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			if (reason != null) {
				return ResultStatus.error(reason.getHint());
			}
		}
		return ResultStatus.error("Could not login user.");
	}

	public boolean isLoggedIn(final String userName) throws NoPeerConnectionException {
		return h2hUserManager.isLoggedIn(userName);
	}

	public ResultStatus logoutUser() throws InvalidProcessStateException, InterruptedException,
			NoPeerConnectionException, NoSessionException {
		IProcessComponent logoutProcess = h2hUserManager.logout();
		ProcessComponentListener listener = new ProcessComponentListener();
		logoutProcess.attachListener(listener);
		logoutProcess.start().await();

		if (listener.hasSucceeded()) {
			return ResultStatus.ok();
		}
		if (listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			if (reason != null) {
				return ResultStatus.error(reason.getHint());
			}
		}
		return ResultStatus.error("Could not logout user.");
	}
}

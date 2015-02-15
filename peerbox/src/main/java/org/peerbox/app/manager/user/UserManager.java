package org.peerbox.app.manager.user;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.peerbox.ResultStatus;
import org.peerbox.app.manager.AbstractManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.events.MessageBus;
import org.peerbox.utils.AppData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public final class UserManager extends AbstractManager implements IUserManager {

	private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

	private UserCredentials userCredentials;

	@Inject
	public UserManager(final INodeManager nodeManager, final MessageBus messageBus) {
		super(nodeManager, messageBus);
	}

	@Override
	public synchronized ResultStatus registerUser(final String username, final String password, final String pin)
			throws NoPeerConnectionException {

		logger.debug("REGISTER - Username: {}", username);

		ResultStatus res = ResultStatus.error("Could not register user.");

		UserCredentials credentials = new UserCredentials(username, password, pin);

		try {

			IProcessComponent<Void> registerProc = getH2HUserManager().createRegisterProcess(credentials);
			registerProc.execute();
			if (isRegistered(credentials.getUserId())) {
				res = ResultStatus.ok();
				notifyRegister(username);
			}

		} catch (ProcessExecutionException | InvalidProcessStateException pex) {
			logger.warn("Register process failed (user={})", username, pex);
		}

		return res;
	}

	private void notifyRegister(final String username) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new RegisterMessage(username));
		}
	}

	@Override
	public boolean isRegistered(final String userName) throws NoPeerConnectionException {
		return getH2HUserManager().isRegistered(userName);
	}

	@Override
	public synchronized ResultStatus loginUser(final String username, final String password, final String pin,
			final Path rootPath) throws NoPeerConnectionException {

		logger.debug("LOGIN - Username: {}", username);

		ResultStatus res = ResultStatus.error("Could not login user.");

		UserCredentials credentials = new UserCredentials(username, password, pin);
		IFileAgent fileAgent = new FileAgent(rootPath, AppData.getCacheFolder());

		try {

			IProcessComponent<Void> loginProc = getH2HUserManager().createLoginProcess(credentials, fileAgent);
			loginProc.execute();
			if (isLoggedIn()) {
				res = ResultStatus.ok();
				userCredentials = credentials;
				notifyLogin();
			}

		} catch (ProcessExecutionException | InvalidProcessStateException pex) {
			logger.warn("Login process failed (user={}).", username, pex);
		}

		return res;
	}

	private void notifyLogin() {
		if (getMessageBus() != null) {
			String userId = null;
			if (userCredentials != null) {
				userId = userCredentials.getUserId();
			}
			getMessageBus().publish(new LoginMessage(userId));
		}
	}

	@Override
	public boolean isLoggedIn() throws NoPeerConnectionException {
		return getH2HUserManager().isLoggedIn();
	}

	@Override
	public synchronized ResultStatus logoutUser() throws NoPeerConnectionException, NoSessionException {

		logger.debug("LOGOUT");

		ResultStatus res = ResultStatus.error("Could not logout user.");

		try {

			IProcessComponent<Void> logoutProc = getH2HUserManager().createLogoutProcess();
			logoutProc.execute();
			if (!isLoggedIn()) {
				res = ResultStatus.ok();
				notifyLogout();
				userCredentials = null;
			}

		} catch (ProcessExecutionException | InvalidProcessStateException pex) {
			String userId = null;
			if(userCredentials != null) {
				userId = userCredentials.getUserId();
			}
			logger.warn("Logout process failed (user={}).", userId);
		}

		return res;
	}

	private void notifyLogout() {
		if (getMessageBus() != null) {
			String userId = null;
			if (userCredentials != null) {
				userId = userCredentials.getUserId();
			}
			getMessageBus().publish(new LogoutMessage(userId));
		}
	}

}

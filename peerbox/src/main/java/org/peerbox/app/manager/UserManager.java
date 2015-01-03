package org.peerbox.app.manager;

import java.io.IOException;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.peerbox.ResultStatus;
import org.peerbox.h2h.FileAgent;
import org.peerbox.h2h.ProcessListener;
import org.peerbox.utils.AppData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public final class UserManager extends AbstractManager implements IUserManager {

	private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

	private UserCredentials userCredentials;
	
	@Inject
	public UserManager(final IH2HManager h2hManager) {
		super(h2hManager);
	}
	
	@Override
	public ResultStatus registerUser(final String username, final String password, final String pin) throws NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("REGISTER - Username: {}", username);
		UserCredentials credentials = new UserCredentials(username, password, pin);
		IProcessComponent<Void> registerProcess = getUserManager().createRegisterProcess(credentials);
		ProcessListener listener = new ProcessListener();
		registerProcess.attachListener(listener);
		
		// TODO: catch exception here and wrap it in code
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

	@Override
	public boolean isRegistered(final String userName) throws NoPeerConnectionException {
		return getUserManager().isRegistered(userName);
	}

	@Override
	public ResultStatus loginUser(final String username, final String password, final String pin,
			final Path rootPath) throws NoPeerConnectionException,
			InvalidProcessStateException, ProcessExecutionException, IOException {
		// TODO what if already logged in?
		logger.debug("LOGIN - Username: {}", username);
		userCredentials = new UserCredentials(username, password, pin);
		IFileAgent fileAgent = new FileAgent(rootPath, AppData.getCacheFolder());

		IProcessComponent<Void> loginProcess = getUserManager().createLoginProcess(userCredentials, fileAgent);
		ProcessListener listener = new ProcessListener();
		loginProcess.attachListener(listener);
		
		// TODO: catch exception here and wrap it in code
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

	@Override
	public boolean isLoggedIn() throws NoPeerConnectionException {
		return getUserManager().isLoggedIn();
	}

	@Override
	public ResultStatus logoutUser() throws InvalidProcessStateException, ProcessExecutionException, 
			NoPeerConnectionException, NoSessionException {
		logger.debug("LOGOUT");
		IProcessComponent<Void> logoutProcess = getUserManager().createLogoutProcess();
		ProcessListener listener = new ProcessListener();
		logoutProcess.attachListener(listener);
		// TODO: catch exception here and wrap it in code
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

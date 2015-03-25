package org.peerbox.app.manager.user;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.ResultStatus;

/**
 * The user manager wraps the H2H user manager (see {@link org.hive2hive.core.api.interfaces.IUserManager}).
 *
 * @author albrecht
 *
 */
public interface IUserManager {

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IUserManager#createRegisterProcess(org.hive2hive.core.security.UserCredentials)}.
	 *
	 * @param username
	 * @param password
	 * @param pin
	 * @return result status
	 * @throws NoPeerConnectionException
	 */
	ResultStatus registerUser(final String username, final String password, final String pin)
			throws NoPeerConnectionException;

	/**
	 * Checks whether a user name is already taken / registered.
	 *
	 * @param userName
	 * @return true if user already registered. False otherwise.
	 * @throws NoPeerConnectionException
	 */
	boolean isRegistered(final String userName) throws NoPeerConnectionException;

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IUserManager#createLoginProcess(
	 * org.hive2hive.core.security.UserCredentials, org.hive2hive.core.file.IFileAgent)}
	 *
	 * @param username
	 * @param password
	 * @param pin
	 * @param rootPath
	 * @return result status
	 * @throws NoPeerConnectionException
	 */
	ResultStatus loginUser(final String username, final String password, final String pin, final Path rootPath)
			throws NoPeerConnectionException;

	/**
	 * Checks whether a user is logged in.
	 *
	 * @return true if user logged in. False otherwise.
	 * @throws NoPeerConnectionException
	 */
	boolean isLoggedIn() throws NoPeerConnectionException;

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IUserManager#createLogoutProcess()}
	 * @return result status
	 * @throws NoPeerConnectionException
	 * @throws NoSessionException
	 */
	ResultStatus logoutUser()
			throws NoPeerConnectionException, NoSessionException;
}

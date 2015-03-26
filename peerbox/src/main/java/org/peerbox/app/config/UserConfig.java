package org.peerbox.app.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.peerbox.utils.OsUtils;
import org.peerbox.utils.WinRegistry;

import com.google.inject.Singleton;

/**
 * Configuration of several user related properties such as user names and path variables.
 * Changes are written to disk (config file) as soon as a value is set or updated.
 *
 * @Singleton: At the moment, the user configuration is maintained as a singleton because of the
 * fixed file names (and writing to the file needs to be synchronized)
 *
 * @author albrecht
 *
 */
@Singleton
public class UserConfig extends AbstractConfig {

	/**
	 * The default configuration (note: resource, not a file)
	 */
	private static final String DEFAULT_PROPERTIES_FILENAME = "/config/default_user";

	/**
	 * The property names
	 */
	private static final String PROPERTY_USERNAME = "username";
	private static final String PROPERTY_PASSWORD = "password";
	private static final String PROPERTY_PIN = "pin";
	private static final String PROPERTY_ROOTPATH = "rootpath";
	private static final String PROPERTY_AUTO_LOGIN = "autologin";


	/**
	 * Creates a new user configuration using the given file name.
	 *
	 * @param file pointing to the property file
	 */
	public UserConfig(final Path file) {
		super(file);
	}

	@Override
	protected String getDefaultPropertiesResource() {
		return DEFAULT_PROPERTIES_FILENAME;
	}

	/**
	 * @return root path
	 */
	public synchronized Path getRootPath() {
		String p = getProperty(PROPERTY_ROOTPATH);
		return hasRootPath() ? Paths.get(p) : null;
	}

	/**
	 * @return true if there is a root path set, false otherwise.
	 */
	public synchronized boolean hasRootPath() {
		return getProperty(PROPERTY_ROOTPATH) != null && !getProperty(PROPERTY_ROOTPATH).isEmpty();
	}

	/**
	 * Sets the root path.
	 *
	 * @param path if null or empty, the root path is removed from the config.
	 * @throws IOException if saving fails.
	 */
	public synchronized void setRootPath(final Path path) throws IOException {
		if (path != null && !path.toString().isEmpty()) {

			setProperty(PROPERTY_ROOTPATH, path.toString());

			if(OsUtils.isWindows()) {
				WinRegistry.setRootPath(path);
			}

		} else {
			removeProperty(PROPERTY_ROOTPATH);
		}
		saveProperties();
	}

	/**
	 * @return the username
	 */
	public synchronized String getUsername() {
		String n = getProperty(PROPERTY_USERNAME);
		return n != null ? n.trim() : n;
	}

	/**
	 * @return true if there is a username set, false otherwise.
	 */
	public synchronized boolean hasUsername() {
		return getUsername() != null && !getUsername().isEmpty();
	}

	/**
	 * Sets the username. Username is trimmed.
	 *
	 * @param username if null or empty, the username is removed from the config.
	 * @throws IOException if saving fails.
	 */
	public synchronized void setUsername(final String username) throws IOException {
		if(username != null && !username.isEmpty()) {
			setProperty(PROPERTY_USERNAME, username.trim());
		} else {
			removeProperty(PROPERTY_USERNAME);
		}
		saveProperties();
	}

	/**
	 * @return the password
	 */
	public synchronized String getPassword() {
		return getProperty(PROPERTY_PASSWORD);
	}

	/**
	 * @return true if there is a password set, false otherwise.
	 */
	public synchronized boolean hasPassword() {
		return getPassword() != null && !getPassword().isEmpty();
	}

	/**
	 * Sets the password.
	 *
	 * @param password if null or empty, the password is removed from the config.
	 * @throws IOException if saving fails.
	 */
	public synchronized void setPassword(final String password) throws IOException {
		if (password != null && !password.isEmpty()) {
			setProperty(PROPERTY_PASSWORD, password);
		} else {
			removeProperty(PROPERTY_PASSWORD);
		}
		saveProperties();
	}

	/**
	 * @return the pin
	 */
	public synchronized String getPin() {
		return getProperty(PROPERTY_PIN);
	}

	/**
	 * @return true if there is a pin set, false otherwise.
	 */
	public synchronized boolean hasPin() {
		return getPin() != null && !getPin().isEmpty();
	}

	/**
	 * Sets the pin.
	 *
	 * @param pin if null or empty, the password is removed from the config.
	 * @throws IOException if saving fails.
	 */
	public synchronized void setPin(final String pin) throws IOException {
		if(pin != null && !pin.isEmpty()) {
			setProperty(PROPERTY_PIN, pin);
		} else {
			removeProperty(PROPERTY_PIN);
		}
		saveProperties();
	}

	/**
	 * @return true if auto login is set, false otherwise.
	 */
	public synchronized boolean isAutoLoginEnabled() {
		return Boolean.valueOf(getProperty(PROPERTY_AUTO_LOGIN));
	}

	/**
	 * Sets the tray notification property.
	 *
	 * @param enabled true if auto login should be enabled. False otherwise.
	 * @throws IOException if saving fails.
	 */
	public synchronized void setAutoLogin(boolean enabled) throws IOException {
		setProperty(PROPERTY_AUTO_LOGIN, Boolean.toString(enabled));
		saveProperties();
	}

}

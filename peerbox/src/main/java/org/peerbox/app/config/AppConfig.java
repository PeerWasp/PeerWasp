package org.peerbox.app.config;

import java.io.IOException;
import java.nio.file.Path;

import org.peerbox.utils.NetUtils;
import org.peerbox.utils.OsUtils;
import org.peerbox.utils.WinRegistry;

import com.google.inject.Singleton;

/**
 * Configuration of several application related properties such as port variables.
 * Changes are written to disk (config file) as soon as a value is set or updated.
 *
 * @Singleton: At the moment, the user configuration is maintained as a singleton because of the
 * fixed file names (and writing to the file needs to be synchronized)
 *
 * @author albrecht
 *
 */
@Singleton
public class AppConfig extends AbstractConfig {

	/**
	 * The default configuration (note: resource, not a file)
	 */
	private static final String DEFAULT_PROPERTIES_FILENAME = "/config/default_app";

	/**
	 * Property names
	 */
	private static final String PROPERTY_API_SERVER_PORT = "api_server_port";


	public AppConfig(Path file) {
		super(file);
	}

	@Override
	protected String getDefaultPropertiesResource() {
		return DEFAULT_PROPERTIES_FILENAME;
	}

	/**
	 * @return the port of the rest api server
	 */
	public synchronized int getApiServerPort() {
		String p = getProperty(PROPERTY_API_SERVER_PORT);
		return p != null ? Integer.valueOf(p.trim()) : -1;
	}

	/**
	 * @return true if there is a server port is set, false otherwise.
	 */
	public synchronized boolean hasApiServerPort() {
		int port = getApiServerPort();
		return NetUtils.isValidPort(port);
	}

	/**
	 * Sets the api server port.
	 *
	 * @param port if not in valid range, the port is removed from the config.
	 * @throws IOException
	 */
	public synchronized void setApiServerPort(final int port) throws IOException {
		if(NetUtils.isValidPort(port)) {

			setProperty(PROPERTY_API_SERVER_PORT, String.valueOf(port));

			if(OsUtils.isWindows()) {
				WinRegistry.setApiServerPort(port);
			}

		} else {
			removeProperty(PROPERTY_API_SERVER_PORT);
		}
		saveProperties();
	}

}

package org.peerbox.app.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class offers functionality for reading and writing simple configuration
 * properties (key value pairs) which are stored in a text file.
 * It is based on Java Properties (java.util.Properties).
 *
 * @author albrecht
 *
 */
abstract class AbstractConfig {
	private static final Logger logger = LoggerFactory.getLogger(AbstractConfig.class);

	/**
	 * Separator character for the serialization of lists of values
	 */
	protected static final String LIST_SEPARATOR = ",";


	private final Path propertyFile;
	private Properties properties;

	protected AbstractConfig(final Path file) {
		this.propertyFile = file;
	}

	/**
	 * Returns the resource path to default properties.
	 *
	 * @return resource path
	 */
	protected abstract String getDefaultPropertiesResource();

	/**
	 * @return the path to the config file
	 */
	public Path getConfigFile() {
		return propertyFile;
	}

	/**
	 * @return the property instance
	 */
	protected Properties getProperties() {
		return properties;
	}

	/**
	 * Returns the value of a property
	 *
	 * @param key
	 * @return value
	 */
	protected String getProperty(String key) {
		checkLoaded();
		return properties.getProperty(key);
	}

	/**
	 * Sets the value of a property
	 *
	 * @param key
	 * @param value
	 */
	protected void setProperty(String key, String value) {
		checkLoaded();
		properties.setProperty(key, value);
	}

	/**
	 * Removes a property
	 *
	 * @param key
	 */
	protected void removeProperty(String key) {
		checkLoaded();
		properties.remove(key);
	}

	/**
	 * Loads the default and user properties from disk
	 *
	 * @throws IOException if loading of file fails
	 */
	public synchronized void load() throws IOException {
		if (propertyFile == null) {
			throw new IllegalStateException(
					"No filename for the user config file given (propertyFile = null)");
		}

		// first read defaults
		Properties defaultProp = loadDefaultProperties();

		// create parent dirs and empty file if not exists yet
		if (!Files.exists(propertyFile)) {
			if (!Files.exists(propertyFile.getParent())) {
				Files.createDirectories(propertyFile.getParent());
			}
			Files.createFile(propertyFile);
		}
		properties = loadUserProperties(defaultProp);
		logger.info("Loaded property file: {}", propertyFile.toAbsolutePath());
	}

	/**
	 * Stores the current properties on disk
	 *
	 * @throws IOException
	 */
	protected synchronized void saveProperties() throws IOException {
		checkLoaded();
		try (OutputStream out = new FileOutputStream(propertyFile.toFile())) {
			properties.store(out, null);
		}
	}

	/**
	 * Loads the default properties
	 *
	 * @return default properties instance
	 * @throws IOException
	 */
	private Properties loadDefaultProperties() throws IOException {
		try (InputStream in = getClass().getResourceAsStream(getDefaultPropertiesResource())) {
			Properties defaultProps = new Properties();
			defaultProps.load(in);
			return defaultProps;
		}
	}

	/**
	 * Loads the user properties. The default and user properties are merged
	 *
	 * @param defaultProp default properties for merging config
	 * @return user properties instance
	 * @throws IOException
	 */
	private Properties loadUserProperties(final Properties defaultProp) throws IOException {
		try (InputStream in = new FileInputStream(propertyFile.toFile())) {
			Properties p = new Properties(defaultProp);
			p.load(in);
			return p;
		}
	}

	private void checkLoaded() {
		if (properties == null) {
			throw new IllegalStateException(
					"Cannot access properties of config. Config file not loaded yet.");
		}
	}

}

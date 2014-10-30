package org.peerbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 * Configuration of several application properties such as user names and path variables.
 * Changes are written to disk (config file) as soon as a value is set or updated.
 * 
 * @Singleton: At the moment, the user configuration is maintained as a singleton because of the 
 * fixed file names (and writing to the file needs to be synchronized)
 * 
 * @author albrecht
 *
 */
@Singleton
public class UserConfig {

	private static final Logger logger = LoggerFactory.getLogger(UserConfig.class);
	
	/**
	 * The default configuration (note: resource, not a file)
	 */
	private static final String DEFAULT_PROPERTIES_FILENAME = "/properties/default";
	/**
	 * The user configuration file (note: file)
	 * TODO: save somewhere safe (e.g. where settings are stored, maybe hidden)
	 */
	private static final String USER_PROPERTIES_FILENAME = Paths.get(FileUtils.getUserDirectoryPath(), ".PeerBox", "peerbox.properties").toString();

	/**
	 * The property names
	 */
	private static final String PROPERTY_BOOTSTRAPPING_NODES = "bootstrappingnodes";
	private static final String PROPERTY_AUTO_LOGIN = "autologin";
	private static final String PROPERTY_USERNAME = "username";
	private static final String PROPERTY_PASSWORD = "password";
	private static final String PROPERTY_PIN = "pin";
	private static final String PROPERTY_ROOTPATH = "rootpath";
	/**
	 * Separator character for the serialization of lists of values
	 */
	private static final String LIST_SEPARATOR = ",";
	
	private File propertyFile;
	private Properties prop;

	/**
	 * Creates a new user configuration using the default configuration
	 * @throws IOException if access to property file fails
	 */
	public UserConfig() throws IOException {
		this(USER_PROPERTIES_FILENAME);
	}
	
	/**
	 * Creates a new user configuration using the given file name.
	 * @param filename the filename of the property file
	 * @throws IOException if access to property file fails
	 */
	private UserConfig(final String filename) throws IOException {
		this.propertyFile = new File(filename);
		loadProperties();
	}

	/**
	 * Loads the default and user properties from disk
	 * @throws IOException
	 */
	private void loadProperties() throws IOException {
		// first read defaults
		Properties defaultProp = loadDefaultProperties();
		// create parent dirs and empty file if not exists yet
		if (!propertyFile.exists()) {
			if (!propertyFile.getParentFile().exists()) {
				propertyFile.getParentFile().mkdirs();
			}
			propertyFile.createNewFile();
		}
		prop = loadUserProperties(defaultProp);
		logger.debug("Loaded property file {}", propertyFile.getAbsoluteFile());
	}
	
	/**
	 * Stores the current properties on disk 
	 * @throws IOException
	 */
	private synchronized void saveProperties() throws IOException {
		try (OutputStream out = new FileOutputStream(propertyFile)) {
			prop.store(out, null);
		}
	}

	/**
	 * Loads the default properties 
	 * @return default properties instance
	 * @throws IOException
	 */
	private Properties loadDefaultProperties() throws IOException {
		try (InputStream in = getClass().getResourceAsStream(DEFAULT_PROPERTIES_FILENAME)) {
			Properties defaultProps = new Properties();
			defaultProps.load(in);
			return defaultProps;
		}
	}

	/**
	 * Loads the user properties. The default and user properties are merged 
	 * @param defaultProp default properties for merging config
	 * @return user properties instance
	 * @throws IOException
	 */
	private Properties loadUserProperties(final Properties defaultProp) throws IOException {
		try (InputStream in = new FileInputStream(propertyFile)) {
			Properties p = new Properties(defaultProp);
			p.load(in);
			return p;
		}
	}

	/**
	 * @return root path
	 */
	public Path getRootPath() {
		String p = prop.getProperty(PROPERTY_ROOTPATH);
		return hasRootPath() ? Paths.get(p) : null;
	}

	/**
	 * @return true if there is a root path set, false otherwise.
	 */
	public boolean hasRootPath() {
		return prop.getProperty(PROPERTY_ROOTPATH) != null && !prop.getProperty("rootpath").isEmpty();
	}

	/**
	 * Sets the root path. 
	 * @param path if null or empty, the root path is removed from the config.
	 * @throws IOException
	 */
	public synchronized void setRootPath(final String path) throws IOException {
		if (path != null && !path.isEmpty()) {
			prop.setProperty(PROPERTY_ROOTPATH, path);
		} else {
			prop.remove(PROPERTY_ROOTPATH);
		}
		saveProperties();
	}

	/**
	 * @return the username 
	 */
	public String getUsername() {
		String n = prop.getProperty(PROPERTY_USERNAME);
		return n != null ? n.trim() : n;
	}
	
	/**
	 * @return true if there is a username set, false otherwise.
	 */
	public boolean hasUsername() {
		return getUsername() != null && !getUsername().isEmpty();
	}
	
	/**
	 * Sets the username. Username is trimmed.
	 * @param username if null or empty, the username is removed from the config.
	 * @throws IOException
	 */
	public synchronized void setUsername(final String username) throws IOException {
		if(username != null && !username.isEmpty()) {
			prop.setProperty(PROPERTY_USERNAME, username.trim());
		} else {
			prop.remove(PROPERTY_USERNAME);
		}
		saveProperties();
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		return prop.getProperty(PROPERTY_PASSWORD);
	}
	
	/**
	 * @return true if there is a password set, false otherwise.
	 */
	public boolean hasPassword() {
		return getPassword() != null && !getPassword().isEmpty();
	}
	
	/**
	 * Sets the password.
	 * @param password if null or empty, the password is removed from the config.
	 * @throws IOException
	 */
	public synchronized void setPassword(final String password) throws IOException {
		if (password != null && !password.isEmpty()) {
			prop.setProperty(PROPERTY_PASSWORD, password);
		} else {
			prop.remove(PROPERTY_PASSWORD);
		}
		saveProperties();
	}
	
	/**
	 * @return the pin
	 */
	public String getPin() {
		return prop.getProperty(PROPERTY_PIN);
	}
	
	/**
	 * @return true if there is a pin set, false otherwise.
	 */
	public boolean hasPin() {
		return getPin() != null && !getPin().isEmpty();
	}
	
	/**
	 * Sets the pin.
	 * @param pin if null or empty, the password is removed from the config.
	 * @throws IOException
	 */
	public synchronized void setPin(final String pin) throws IOException {
		if(pin != null && !pin.isEmpty()) {
			prop.setProperty(PROPERTY_PIN, pin);
		} else {
			prop.remove(PROPERTY_PIN);
		}
		saveProperties();
	}
	
	/**
	 * @return true if auto login is set, false otherwise.
	 */
	public boolean isAutoLoginEnabled() {
		return Boolean.valueOf(prop.getProperty(PROPERTY_AUTO_LOGIN));
	}
	
	/**
	 * Sets the auto login property.
	 * @param enabled
	 * @throws IOException
	 */
	public synchronized void setAutoLogin(boolean enabled) throws IOException {
		prop.setProperty(PROPERTY_AUTO_LOGIN, Boolean.toString(enabled));
		saveProperties();
	}
	
	/**
	 * @return a list of addresses of bootstrapping nodes 
	 */
	public List<String> getBootstrappingNodes() {
		List<String> nodes = new ArrayList<String>();
		if (hasBootstrappingNodes()) {
			String nodesCsv = prop.getProperty(PROPERTY_BOOTSTRAPPING_NODES);
			String nodesArray[] = nodesCsv.split(LIST_SEPARATOR);
			for (String n : nodesArray) {
				if (!n.trim().isEmpty())
					nodes.add(n);
			}
		}
		return nodes;
	}
	
	/**
	 * @return true if there is at least one address stored, false otherwise.
	 */
	public boolean hasBootstrappingNodes() {
		String s = prop.getProperty(PROPERTY_BOOTSTRAPPING_NODES);
		return s != null && !s.trim().isEmpty();
	}
	
	/**
	 * Sets a list of bootstrapping node addresses. Overrides old addresses.
	 * Addresses are trimmed.
	 * @param nodes list of addresses.
	 * @throws IOException
	 */
	public synchronized void setBootstrappingNodes(final List<String> nodes) throws IOException {
		if(nodes == null || nodes.isEmpty()) {
			prop.remove(PROPERTY_BOOTSTRAPPING_NODES);
			saveProperties();
			return;
		}
		
		StringBuilder nodeList = new StringBuilder();
		Set<String> uniqueNodes = new HashSet<String>();
		for (String node : nodes) {
			String n = node != null ? node.trim() : null;
			if (n != null && !n.isEmpty() && !uniqueNodes.contains(n)) {
				nodeList.append(n).append(LIST_SEPARATOR);
				uniqueNodes.add(n);
			}
		}
		// delete trailing separator
		if (!nodes.isEmpty() && nodeList.length() > 0) {
			nodeList.deleteCharAt(nodeList.length() - 1);
		}
		prop.setProperty(PROPERTY_BOOTSTRAPPING_NODES, nodeList.toString());
		saveProperties();
	}
	
	/**
	 * Adds an address of a bootstrapping node to the current list 
	 * @param node address of a node
	 * @throws IOException
	 */
	public synchronized void addBootstrapNode(final String node) throws IOException {
		if(node != null && !node.isEmpty()) {
			List<String> nodes = getBootstrappingNodes();
			nodes.add(node);
			setBootstrappingNodes(nodes);
		}
	}
	
	/**
	 * Removes an address of a bootstrapping node from the current list.
	 * @param node address of a node
	 * @throws IOException
	 */
	public synchronized void removeBootstrapNode(final String node) throws IOException {
		if(node != null && !node.isEmpty()) {
			List<String> nodes = getBootstrappingNodes();
			nodes.remove(node);
			setBootstrappingNodes(nodes);
		}
	}
}

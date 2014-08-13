package org.peerbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 * 
 * @author albrecht
 *
 */
@Singleton
public class UserConfig {

	private static final Logger logger = LoggerFactory.getLogger(UserConfig.class);
	
	private static final String DEFAULT_PROPERTIES_FILENAME = "/properties/default";
	private static final String USER_PROPERTIES_FILENAME = "peerbox.properties";

	private static final String PROPERTY_BOOTSTRAPPING_NODES = "bootstrappingnodes";
	private static final String PROPERTY_AUTO_LOGIN = "autologin";
	private static final String PROPERTY_USERNAME = "username";
	private static final String PROPERTY_PASSWORD = "password";
	private static final String PROPERTY_PIN = "pin";
	private static final String PROPERTY_ROOTPATH = "rootpath";

	private static final String LIST_SEPARATOR = ",";

	private File propertyFile;
	private Properties prop;

	public UserConfig() throws IOException {
		this(USER_PROPERTIES_FILENAME);
	}

	private UserConfig(final String filename) throws IOException {
		this.propertyFile = new File(filename);
		loadProperties();
	}

	// check if property file is already existing in project folder
	private void loadProperties() throws IOException {
		// first read defaults
		Properties defaultProp = loadDefaultProperties();
		// prop = new Properties(defaultProp);
		// create empty file if not exists yet
		if (!propertyFile.exists()) {
			propertyFile.createNewFile();
		}
		prop = loadCustomProperties(defaultProp);
		logger.debug("Loaded property file {}", propertyFile.getAbsoluteFile());
	}

	private synchronized void saveProperties() throws IOException {
		try (OutputStream out = new FileOutputStream(propertyFile)) {
			prop.store(out, null);
		}
	}

	private Properties loadDefaultProperties() throws IOException {
		try (InputStream in = getClass().getResourceAsStream(DEFAULT_PROPERTIES_FILENAME)) {
			Properties defaultProps = new Properties();
			defaultProps.load(in);
			return defaultProps;
		}
	}

	// load existing property file
	private Properties loadCustomProperties(final Properties defaultProp) throws IOException {
		try (InputStream in = new FileInputStream(propertyFile)) {
			Properties p = new Properties(defaultProp);
			p.load(in);
			return p;
		}
	}

	// returns rootpath value from property file
	public Path getRootPath() {
		String p = prop.getProperty(PROPERTY_ROOTPATH);
		return hasRootPath() ? Paths.get(p) : null;
	}

	// check whether the property file already holds a rootpath property
	public boolean hasRootPath() {
		return prop.getProperty(PROPERTY_ROOTPATH) != null && !prop.getProperty("rootpath").isEmpty();
	}

	// write root path from SelectRootPathController to property file
	public synchronized void setRootPath(final String path) throws IOException {
		if (path != null && !path.isEmpty()) {
			prop.setProperty(PROPERTY_ROOTPATH, path);
		} else {
			prop.remove(PROPERTY_ROOTPATH);
		}
		saveProperties();
	}

	public String getUsername() {
		String n = prop.getProperty(PROPERTY_USERNAME);
		return n != null ? n.trim() : n;
	}

	public boolean hasUsername() {
		return getUsername() != null && !getUsername().isEmpty();
	}

	public synchronized void setUsername(final String username) throws IOException {
		if(username != null && !username.isEmpty()) {
			prop.setProperty(PROPERTY_USERNAME, username.trim());
		} else {
			prop.remove(PROPERTY_USERNAME);
		}
		saveProperties();
	}

	public String getPassword() {
		return prop.getProperty(PROPERTY_PASSWORD);
	}

	public boolean hasPassword() {
		return getPassword() != null && !getPassword().isEmpty();
	}

	public synchronized void setPassword(final String password) throws IOException {
		if (password != null && !password.isEmpty()) {
			prop.setProperty(PROPERTY_PASSWORD, password);
		} else {
			prop.remove(PROPERTY_PASSWORD);
		}
		saveProperties();
	}

	public String getPin() {
		return prop.getProperty(PROPERTY_PIN);
	}

	public boolean hasPin() {
		return getPin() != null && !getPin().isEmpty();
	}

	public synchronized void setPin(final String pin) throws IOException {
		if(pin != null && !pin.isEmpty()) {
			prop.setProperty(PROPERTY_PIN, pin);
		} else {
			prop.remove(PROPERTY_PIN);
		}
		saveProperties();
	}

	public boolean isAutoLoginEnabled() {
		return Boolean.valueOf(prop.getProperty(PROPERTY_AUTO_LOGIN));
	}

	public synchronized void setAutoLogin(boolean enabled) throws IOException {
		prop.setProperty(PROPERTY_AUTO_LOGIN, Boolean.toString(enabled));
		saveProperties();
	}

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

	public boolean hasBootstrappingNodes() {
		String s = prop.getProperty(PROPERTY_BOOTSTRAPPING_NODES);
		return s != null && !s.trim().isEmpty();
	}

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

	public synchronized void addBootstrapNode(final String node) throws IOException {
		if(node != null && !node.isEmpty()) {
			List<String> nodes = getBootstrappingNodes();
			nodes.add(node);
			setBootstrappingNodes(nodes);
		}
	}

	public synchronized void removeBootstrapNode(final String node) throws IOException {
		if(node != null && !node.isEmpty()) {
			List<String> nodes = getBootstrappingNodes();
			nodes.remove(node);
			setBootstrappingNodes(nodes);
		}
	}

}

package org.peerbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(PropertyHandler.class);
	
	private static final String DEFAULT_PROPERTIES_FILENAME = "/properties/default";
	private static final String FILENAME = "peerbox.properties";
	
	private static final String PROPERTY_BOOTSTRAPPING_NODES = "bootstrappingnodes";
	private static final String PROPERTY_AUTO_LOGIN = "autologin";
	private static final String PROPERTY_USERNAME = "username";
	private static final String PROPERTY_PASSWORD = "password";
	private static final String PROPERTY_PIN = "pin";
	private static final String PROPERTY_ROOTPATH = "rootpath";
	
	private static final String LIST_SEPARATOR = ",";
	
	private File propertyFile;
	private Properties prop;
	
	public PropertyHandler() throws IOException {
		this(FILENAME);
	}
	
	private PropertyHandler(String filename) throws IOException {
		this.propertyFile = new File(filename);
		loadProperties();
	}

	//check if property file is already existing in project folder
	private void loadProperties() throws IOException{
		// first read defaults
		Properties defaultProp = loadDefaultProperties();
//		prop = new Properties(defaultProp);
		// create empty file if not exists yet
		if(!propertyFile.exists()) {
			propertyFile.createNewFile();
		}
		prop = loadCustomProperties(defaultProp);
		logger.debug("Loaded property file {}", propertyFile.getAbsoluteFile());
	}
		
	private void saveProperties() throws IOException {
		try(OutputStream out = new FileOutputStream(propertyFile)) {
			prop.store(out, null);
		}
	}
	
	private Properties loadDefaultProperties() throws IOException {
		try(InputStream in = getClass().getResourceAsStream(DEFAULT_PROPERTIES_FILENAME)) {
			Properties defaultProps = new Properties();
			defaultProps.load(in);
			return defaultProps;
		}
	}

	//load existing property file
	private Properties loadCustomProperties(Properties defaultProp) throws IOException{
		try(InputStream in = new FileInputStream(propertyFile)) {
			Properties p = new Properties(defaultProp);
			p.load(in);
			return p;
		}
	}
	
//	//if no property file is found, a new one will be written to the project folder
//	private void createPropertyFile(String filename) throws IOException {
//		try(OutputStream out = new FileOutputStream(filename)) {
//			logger.debug("New property file created: {}", filename);
//			new Properties().store(out, null);
//		}
//	}
	
	
	
	//write root path from SelectRootPathController to property file
	public void setRootPath(String path) throws IOException {
		prop.setProperty(PROPERTY_ROOTPATH, path);
		saveProperties();
		System.out.println("Root path stored in property file.");
	}
	
	//check whether the property file already holds a rootpath property
	public boolean rootPathExists(){
		return prop.getProperty(PROPERTY_ROOTPATH) != null && !prop.getProperty("rootpath").isEmpty();
	}
	
	//returns rootpath value from property file
	public String getRootPath(){
		if(!rootPathExists()){
			prop.setProperty("rootpath", "unset"); // TODO: why set to unset?
		}
		return prop.getProperty(PROPERTY_ROOTPATH);
	}
	
	public String getUsername() {
		String n = prop.getProperty(PROPERTY_USERNAME);
		return n != null ? n.trim() : n;
	}
	
	public boolean hasUsername() {
		return getUsername() != null && !getUsername().isEmpty();
	}
	
	public void setUsername(String username) throws IOException {
		prop.setProperty(PROPERTY_USERNAME, username);
		saveProperties();
	}
	
	public String getPassword() {
		return prop.getProperty(PROPERTY_PASSWORD);
	}
	
	public boolean hasPassword() {
		return getPassword() != null && !getPassword().isEmpty();
	}
	
	public void setPassword(String password) throws IOException {
		prop.setProperty(PROPERTY_PASSWORD, password);
		saveProperties();
	}
	
	public String getPin() {
		return prop.getProperty(PROPERTY_PIN);
	}
	
	public boolean hasPin() {
		return getPin() != null && !getPin().isEmpty();
	}
	
	public void setPin(String pin) throws IOException {
		prop.setProperty(PROPERTY_PIN, pin);
		saveProperties();
	}

	public boolean isAutoLoginEnabled() {
		return Boolean.valueOf(prop.getProperty(PROPERTY_AUTO_LOGIN));
	}

	public void setAutoLogin(boolean enabled) throws IOException {
		prop.setProperty(PROPERTY_AUTO_LOGIN, Boolean.toString(enabled));
		saveProperties();
	}
	
	public boolean hasBootstrappingNodes() {
		String s = prop.getProperty(PROPERTY_BOOTSTRAPPING_NODES);
		return s != null && !s.trim().isEmpty();
	}
	
	public List<String> getBootstrappingNodes() {
		List<String> nodes = new ArrayList<String>();
		if(hasBootstrappingNodes()) {
			String nodesCsv = prop.getProperty(PROPERTY_BOOTSTRAPPING_NODES);
			String nodesArray[] = nodesCsv.split(LIST_SEPARATOR);
			for(String n : nodesArray) {
				if(!n.trim().isEmpty()) nodes.add(n);
			}		
		}
		return nodes;
	}

	public void addBootstrapNode(String node) throws IOException {
		List<String> nodes = getBootstrappingNodes();
		nodes.add(node);
		setBootstrappingNodes(nodes);
	}
	
	public void removeBootstrapNode(String node) throws IOException {
		List<String> nodes = getBootstrappingNodes();
		nodes.remove(node);
		setBootstrappingNodes(nodes);
	}
	
	public void setBootstrappingNodes(List<String> nodes) throws IOException {
		StringBuilder nodeList = new StringBuilder();
		Set<String> uniqueNodes = new HashSet<String>();
		for(String node : nodes) {
			String n = node.trim();
			if(!n.isEmpty() && !uniqueNodes.contains(n)) {
				nodeList.append(n).append(LIST_SEPARATOR);
				uniqueNodes.add(n);
			}
		}
		// delete trailing separator
		if(!nodes.isEmpty() && nodeList.length() > 0) {
			nodeList.deleteCharAt(nodeList.length()-1);
		}
		prop.setProperty(PROPERTY_BOOTSTRAPPING_NODES, nodeList.toString());
		saveProperties();
	}
}


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
	private static final String PROPERTY_AUTO_JOIN = "autojoin";
	private static final String PROPERTY_AUTO_LOGIN = "autologin";
	private static final String PROPERTY_USERNAME = "username";
	private static final String PROPERTY_PASSWORD = "password";
	private static final String PROPERTY_PIN = "pin";
	
	private static final String LIST_SEPARATOR = ",";
	
	
	private static Properties prop;

	//check if property file is already existing in project folder
	public static void loadProperties() throws IOException{
		Properties defaultProp = loadDefaultProperties();
		prop = new Properties(defaultProp);
		//read in property file
		File f = new File(FILENAME);
		if(!f.exists()) {
			createPropertyFile(FILENAME);
		}
		prop = loadCustomProperties(defaultProp);
		logger.debug("Load property file {}", f.getAbsoluteFile());
	}
		
	public static void saveProperties() throws IOException {
		try(OutputStream out = new FileOutputStream(FILENAME)) {
			prop.store(out, null);
		}
	}
	
	private static Properties loadDefaultProperties() throws IOException {
		try(InputStream in = PropertyHandler.class.getResourceAsStream(DEFAULT_PROPERTIES_FILENAME)) {
			Properties defaultProps = new Properties();
			defaultProps.load(in);
			return defaultProps;
		}
	}

	//load existing property file
	private static Properties loadCustomProperties(Properties defaultProp) throws IOException{
		try(InputStream in = new FileInputStream(FILENAME)) {
			Properties p = new Properties(defaultProp);
			p.load(in);
			return p;
		}
	}
	
	//if no property file is found, a new one will be written to the project folder
	private static void createPropertyFile(String filename) throws IOException {
		try(OutputStream out = new FileOutputStream(filename)) {
			prop.store(out, "");
			logger.debug("New property file created: {}", filename);
		}
	}
	
	
	
	//write root path from SelectRootPathController to property file
	public static void setRootPath(String path) throws IOException{
		prop.setProperty("rootpath",path);
		System.out.println("Root path stored in property file.");
		saveProperties();
	}
	
	//check whether the property file already holds a rootpath property
	public static boolean rootPathExists(){
		return prop.getProperty("rootpath") != null && !prop.getProperty("rootpath").isEmpty();
	}
	
	//returns rootpath value from property file
	public static String getRootPath(){
		if(!rootPathExists()){
			prop.setProperty("rootpath", "unset");
		}
		return prop.getProperty("rootpath");
	}
	
	public static String getUsername() {
		String n = prop.getProperty(PROPERTY_USERNAME);
		return n != null ? n.trim() : n;
	}
	
	public static boolean hasUsername() {
		return getUsername() != null && !getUsername().isEmpty();
	}
	
	public static void setUsername(String username) throws IOException {
		prop.setProperty(PROPERTY_USERNAME, username);
		saveProperties();
	}
	
	public static String getPassword() {
		return prop.getProperty(PROPERTY_PASSWORD);
	}
	
	public static boolean hasPassword() {
		return getPassword() != null && !getPassword().isEmpty();
	}
	
	public static void setPassword(String password) throws IOException {
		prop.setProperty(PROPERTY_PASSWORD, password);
		saveProperties();
	}
	
	public static String getPin() {
		return prop.getProperty(PROPERTY_PIN);
	}
	
	public static boolean hasPin() {
		return getPin() != null && !getPin().isEmpty();
	}
	
	public static void setPin(String pin) throws IOException {
		prop.setProperty(PROPERTY_PIN, pin);
		saveProperties();
	}

	public static boolean isAutoLoginEnabled() {
		return Boolean.valueOf(prop.getProperty(PROPERTY_AUTO_LOGIN));
	}

	public static void setAutoLogin(boolean enabled) throws IOException {
		prop.setProperty(PROPERTY_AUTO_LOGIN, Boolean.toString(enabled));
		saveProperties();
	}
	
	public static boolean isAutoJoinEnabled() {
		return Boolean.valueOf(prop.getProperty(PROPERTY_AUTO_JOIN));
	}

	public static void setAutoJoin(boolean enabled) throws IOException {
		prop.setProperty(PROPERTY_AUTO_JOIN, Boolean.toString(enabled));
		saveProperties();
	}
	
	public static boolean hasBootstrappingNodes() {
		String s = prop.getProperty(PROPERTY_BOOTSTRAPPING_NODES);
		return s != null && s.trim().length() > 0;
	}
	
	public static List<String> getBootstrappingNodes() {
		List<String> nodes = new ArrayList<String>();
		if(hasBootstrappingNodes()) {
			String nodesCsv = prop.getProperty(PROPERTY_BOOTSTRAPPING_NODES);
			String nodesArray[] = nodesCsv.split(",");
			for(String n : nodesArray) {
				if(n.trim().length() > 0) nodes.add(n);
			}		
		}
		return nodes;
	}

	public static void addBootstrapNode(String node) throws IOException {
		List<String> nodes = getBootstrappingNodes();
		nodes.add(node);
		setBootstrappingNodes(nodes);
	}
	
	public static void removeBootstrapNode(String node) throws IOException {
		List<String> nodes = getBootstrappingNodes();
		nodes.remove(node);
		setBootstrappingNodes(nodes);
	}
	
	public static void setBootstrappingNodes(List<String> nodes) throws IOException {
		StringBuilder nodeList = new StringBuilder();
		Set<String> uniqueNodes = new HashSet<String>();
		for(String node : nodes) {
			String n = node.trim();
			if(n.length() > 0 && !uniqueNodes.contains(node)) {
				nodeList.append(n).append(LIST_SEPARATOR);
				uniqueNodes.add(node);
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


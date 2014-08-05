package org.peerbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class PropertyHandler {
	
	private static final String PROPERTY_BOOTSTRAPPING_NODES = "bootstrappingnodes";
	private static final String PROPERTY_AUTO_JOIN = "autojoin";
	private static final String PROPERTY_AUTO_LOGIN = "autologin";
	private static final String PROPERTY_USERNAME = "username";
	
	private static final Object LIST_SEPARATOR = ",";
	
	
//	PropertyHandler propHandler = new PropertyHandler();
	private static Properties prop = new Properties();

	private static boolean saveProperties() {
		boolean success = false;
		
		try {
			prop.store(new FileOutputStream("config.properties"), null);
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}


	//check if property file is already existing in project folder
	public static void checkFileExists(){
		
		File f = new File("config.properties");
		if(f.exists() && !f.isDirectory()) { 
			//read in property file
			loadPropertyFile();
			System.out.println("Existing property file found.");
		} else {
			//create new property file if no existing is found
			createPropertyFile();
		}
	}
	
	
	//load existing property file
	public static void loadPropertyFile(){
		
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// load a properties file
		try {
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//if no property file is found, a new one will be written to the project folder
	public static void createPropertyFile(){
    	
		try {
    		//Dummy test set
//    		prop.setProperty("peerAddress", "localhost");
//    		prop.setProperty("username", "myuser");
//    		prop.setProperty("password", "mypwd");
//    		prop.setProperty("pin", "1234");
//    		prop.setProperty("rootpath", "unset");
    		//save properties to project root folder
    		prop.store(new FileOutputStream("config.properties"), null);
    		System.out.println("New property file created.");
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }
    }
	
	//write root path from SelectRootPathController to property file
	public static void setRootPath(String path){
    	try {
    		prop.setProperty("rootpath",path);
    		//save properties to project root folder
    		prop.store(new FileOutputStream("config.properties"),null);
    		System.out.println("Root path stored in property file.");
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }
	}
	
	//check whether the property file already holds a rootpath property
	public static boolean rootPathExists(){		
		if(prop.getProperty("rootpath") != null){
			System.out.println("using rootpath from property file.");
			return true;
		} else {
			return false;
		}
	}
	
	//returns rootpath value from property file
	public static String getRootPath(){
		if(prop.getProperty("rootpath") == null){
			prop.setProperty("rootpath", "unset");
		}
		return prop.getProperty("rootpath");
	}
	
	public static String getUsername() {
		return prop.getProperty(PROPERTY_USERNAME).trim();
	}
	
	public static boolean hasUsername() {
		return getUsername() != null && getUsername().length() > 0;
	}
	
	public static void setUsername(String username) {
		prop.setProperty(PROPERTY_USERNAME, username);
	}

	public static boolean isAutoLoginEnabled() {
		return Boolean.valueOf(prop.getProperty(PROPERTY_AUTO_LOGIN));
	}

	public static void setAutoLogin(boolean enabled) {
		prop.setProperty(PROPERTY_AUTO_LOGIN, Boolean.toString(enabled));
		saveProperties();
	}
	
	public static boolean isAutoJoinEnabled() {
		return Boolean.valueOf(prop.getProperty(PROPERTY_AUTO_JOIN));
	}

	public static void setAutoJoin(boolean enabled) {
		prop.setProperty(PROPERTY_AUTO_JOIN, Boolean.toString(enabled));
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

	public static void addBootstrapNode(String node) {
		List<String> nodes = getBootstrappingNodes();
		nodes.add(node);
		setBootstrappingNodes(nodes);
	}
	
	public static void removeBootstrapNode(String node) {
		List<String> nodes = getBootstrappingNodes();
		nodes.remove(node);
		setBootstrappingNodes(nodes);
	}
	
	public static void setBootstrappingNodes(List<String> nodes) {
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


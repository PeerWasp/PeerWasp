package org.peerbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyHandler {
	
	PropertyHandler propHandler = new PropertyHandler();
	static Properties prop = new Properties();

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
    		prop.setProperty("peerAddress", "localhost");
    		prop.setProperty("username", "myuser");
    		prop.setProperty("password", "mypwd");
    		prop.setProperty("pin", "1234");
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
		String fPath = null;
		fPath = prop.getProperty("rootpath");
		return fPath;
	}
}


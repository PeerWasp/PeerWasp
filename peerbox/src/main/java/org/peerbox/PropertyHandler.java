package org.peerbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertyHandler {
	
	PropertyHandler propHandler = new PropertyHandler();
	static Properties prop = new Properties();

	
	public static void checkFileExists(){
		
		boolean fileExists = false;
		
		File f = new File("config.properties");
		if(f.exists() && !f.isDirectory()) { 
			fileExists = true;
			System.out.println("Existing property file found.");
		} else {
			//create new property file if no existing is found
			createPropertyFile();
		}
	}
	
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
}


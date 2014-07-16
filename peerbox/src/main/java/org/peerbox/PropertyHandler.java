package org.peerbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertyHandler {

	public boolean checkFileExists(){
		
		boolean fileExists = true;
		
		File configFile = new File("config.properties");
		 
		try {
		    FileReader reader = new FileReader(configFile);
		    
		} catch (FileNotFoundException ex) {
		    // file does not exist
			fileExists = false;
			System.out.println("PeerBox property file not found.");
		} catch (IOException ex) {
		    // I/O error
		}
		
		return fileExists;
	}
	
	public void createPropertyFile(){
		
		Properties prop = new Properties();

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
}


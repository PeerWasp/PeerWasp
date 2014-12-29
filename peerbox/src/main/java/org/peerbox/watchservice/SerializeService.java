package org.peerbox.watchservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.peerbox.watchservice.filetree.composite.FolderComposite;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class SerializeService {
	
	private static String fileTreeObj = System.getProperty("user.home") + File.separator + "PeerBoxConfig" + File.separator + "fileTree.dat";
			
    public static void serializeToXml(FolderComposite file){
    	XStream xstream = new XStream(new StaxDriver());
    	String xml = xstream.toXML(file);
    	try {
			FileUtils.writeStringToFile(new File(fileTreeObj), xml);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static FolderComposite deserializeFromXml(){
    	XStream xstream = new XStream(new StaxDriver());
    	Path xmlPath = Paths.get(fileTreeObj);
    	File xmlFile = xmlPath.toFile();
    	String xml = null;
		try {
			xml = FileUtils.readFileToString(xmlFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	FolderComposite fileTree = (FolderComposite)xstream.fromXML(xml);
    	
    	return fileTree;
    	
    }
}

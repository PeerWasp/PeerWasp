package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
	
public class InteractiveFolderWatchServiceTest {
	
	 public static void main(String[] args) throws Exception {
		 String path = System.getProperty("user.home");
		path = path.concat(File.separator + "PeerBox_FolderWatchServiceTest" + File.separator);
		
	 FolderWatchService service = new FolderWatchService(Paths.get(path));
	 service.start();
	 System.out.println("Running");
	
	// Thread.sleep(1000*10);
	// service.stop();
	// System.out.println("Stopping");
	 }
}

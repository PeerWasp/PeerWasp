package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.peerbox.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
	
public class InteractiveFolderWatchServiceTest {
	
	private static final Logger logger = LoggerFactory.getLogger(InteractiveFolderWatchServiceTest.class);
	
	public static void main(String[] args) throws Exception {
		Path path = Paths.get(FileUtils.getTempDirectoryPath(), "PeerBox_FolderWatchServiceTest");
		logger.info("Path: {}", path.toString());
		
		FolderWatchService service = new FolderWatchService(path);
		FileEventManager eventManager = new FileEventManager();
		eventManager.setFileManager(new FileManager());
		service.addFileEventListener(eventManager);
		service.start();
		
		logger.info("Running");

		// Thread.sleep(1000*10);
		// service.stop();
		// System.out.println("Stopping");
	}
}

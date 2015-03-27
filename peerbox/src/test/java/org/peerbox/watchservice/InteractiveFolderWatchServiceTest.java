package org.peerbox.watchservice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
	
public class InteractiveFolderWatchServiceTest {
	
	private static final Logger logger = LoggerFactory.getLogger(InteractiveFolderWatchServiceTest.class);
	
	public static void main(String[] args) throws Exception {
		Path path = Paths.get(FileUtils.getTempDirectoryPath(), "PeerWasp_FolderWatchServiceTest");
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
		logger.info("Path: {}", path.toString());

		FolderWatchService service = new FolderWatchService();
		service.addFileEventListener(new PrintListener());
		// FileEventManager eventManager = new FileEventManager(path);
		// service.addFileEventListener(eventManager);
		service.start(path);

		logger.info("Running");
		
//		Thread.sleep(1000 * 5);
//		service.stop();
//		logger.info("Stopped...");
		

		// recursive cleanup
		// FileUtils.deleteDirectory(path.toFile());
	}
	
	
	
	private static class PrintListener implements ILocalFileEventListener {
		@Override
		public void onLocalFileModified(Path path) {
			logger.info("onLocalFileModified: {}", path);
		}

		@Override
		public void onLocalFileDeleted(Path path) {
			logger.info("onLocalFileDeleted: {}", path);
		}

		@Override
		public void onLocalFileCreated(Path path) {
			logger.info("onLocalFileCreated: {}", path);
		}
	}
}

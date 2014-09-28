package org.peerbox.client;

import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.peerbox.FileManager;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.FolderWatchService;

class ClientNode {
	
	private IH2HNode node;
	private UserCredentials credentials;
	private Path rootPath;
	
	private FileEventManager fileEventManager;
	private FolderWatchService watchService;

	public ClientNode(IH2HNode node, UserCredentials credentials, Path rootPath) throws Exception {
		this.node = node;
		this.credentials = credentials;
		this.rootPath = rootPath;
		
		initialization();
		
	}

	private void initialization() throws Exception {
		// create path
		if (!Files.exists(rootPath)) {
			Files.createDirectory(rootPath);
		}

		// create managers and initialization
		fileEventManager = new FileEventManager(rootPath);
		watchService = new FolderWatchService(rootPath);
		watchService.addFileEventListener(fileEventManager);
		fileEventManager.setFileManager(new FileManager(node.getFileManager()));
		
		node.getFileManager().subscribeFileEvents(fileEventManager);

		// login
		IProcessComponent loginProcess = node.getUserManager().login(credentials, rootPath);
		TestExecutionUtil.executeProcess(loginProcess);

		// start monitoring folder
		watchService.start();
	}
}
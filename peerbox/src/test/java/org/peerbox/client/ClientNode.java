package org.peerbox.client;

import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.TestExecutionUtil;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.mockito.Mockito;
import org.peerbox.FileManager;
import org.peerbox.app.manager.IH2HManager;
import org.peerbox.h2h.FileAgent;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.FolderWatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientNode {
	private static final Logger logger = LoggerFactory.getLogger(ClientNode.class);
	
	private IH2HNode node;
	private UserCredentials credentials;
	private Path rootPath;
	
	private FileEventManager fileEventManager;
	private FileManager fileManager;
	private FolderWatchService watchService;

	public ClientNode(IH2HNode node, UserCredentials credentials, Path rootPath) throws Exception {
		this.node = node;
		this.credentials = credentials;
		this.rootPath = rootPath;
		
		initialization();
		
	}
	
	public Path getRootPath() {
		return rootPath;
	}
	
	public FileEventManager getFileEventManager(){
		return fileEventManager;
	}

	private void initialization() throws Exception {
		// create path
		if (!Files.exists(rootPath)) {
			Files.createDirectory(rootPath);
		}

		// create managers and initialization
		IH2HManager manager = Mockito.mock(IH2HManager.class);
		Mockito.stub(manager.getNode()).toReturn(node);
		fileManager = new FileManager(manager);
		fileEventManager = new FileEventManager(rootPath, true);
		watchService = new FolderWatchService(rootPath);
		watchService.addFileEventListener(fileEventManager);
		
		//TODO remove cycle dependency
		fileEventManager.setFileManager(fileManager);
		
		node.getFileManager().subscribeFileEvents(fileEventManager);

		// login
		logger.debug("Login user {}", credentials.getUserId());
		loginUser();

		// start monitoring folder
		logger.debug("Start watchservice");
		watchService.start();
	}
	
	private void loginUser() throws NoPeerConnectionException {
		FileAgent fileAgent = new FileAgent(rootPath, null);
		IProcessComponent<Void> loginProcess = node.getUserManager().createLoginProcess(credentials, fileAgent);
		TestExecutionUtil.executeProcessTillSucceded(loginProcess);
	}
	
	private void logoutUser() throws NoPeerConnectionException, NoSessionException {
		IProcessComponent<Void> registerProcess = node.getUserManager().createLogoutProcess();
		TestExecutionUtil.executeProcessTillSucceded(registerProcess);
	}
	
	public void stop() {
		try {
			logger.debug("Stop watchservice {}", credentials.getUserId());
			
//			fileEventManager.stopExecutor();
			watchService.stop();
		} catch (Exception e) {
			// ignore
			e.printStackTrace();
		}
		
		try {
			logger.debug("Logout user {}", credentials.getUserId());
			logoutUser();
		} catch (NoPeerConnectionException | NoSessionException e) {
			// ignore
			e.printStackTrace();
		}
		
		
	}
}
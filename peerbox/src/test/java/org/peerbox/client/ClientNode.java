package org.peerbox.client;

import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.peerbox.FileManager;
import org.peerbox.h2h.PeerboxFileAgent;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.FolderWatchService;
import org.peerbox.watchservice.RecoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientNode {
	private static final Logger logger = LoggerFactory.getLogger(ClientNode.class);
	
	private IH2HNode node;
	private UserCredentials credentials;
	private Path rootPath;
	
	private FileEventManager fileEventManager;
	private FileManager fileManager;
	private RecoveryService recoveryService;
	private FolderWatchService watchService;

	public ClientNode(IH2HNode node, UserCredentials credentials, Path rootPath) throws Exception {
		this.node = node;
		this.credentials = credentials;
		this.rootPath = rootPath;
		
		initialization();
		
	}
	
	public RecoveryService getRecoveryService(){
		return recoveryService;
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
		fileManager = new FileManager(node.getFileManager());
		fileEventManager = new FileEventManager(rootPath, true);
		recoveryService = new RecoveryService(fileManager);
		watchService = new FolderWatchService(rootPath);
		watchService.addFileEventListener(fileEventManager);
		
		//TODO remove cycle dependency
		fileEventManager.setFileManager(fileManager);
		fileManager.setFileEventManager(fileEventManager);
		
		node.getFileManager().subscribeFileEvents(fileEventManager);

		// login
		logger.debug("Login user {} (node ̣{})", credentials.getUserId(), node.getNetworkConfiguration().getNodeID());
		loginUser();

		// start monitoring folder
		logger.debug("Start watchservice");
		watchService.start();
	}
	
	private void loginUser() throws NoPeerConnectionException {
		PeerboxFileAgent fileAgent = new PeerboxFileAgent(rootPath.toFile());
		IProcessComponent loginProcess = node.getUserManager().login(credentials, fileAgent);
		TestExecutionUtil.executeProcessTillSucceded(loginProcess);
	}
	
	private void logoutUser() throws NoPeerConnectionException, NoSessionException {
		IProcessComponent registerProcess = node.getUserManager().logout();
		TestExecutionUtil.executeProcessTillSucceded(registerProcess);
	}
	
	public void stop() {
		try {
			logger.debug("Stop watchservice {} (node ̣{})", credentials.getUserId(), node.getNetworkConfiguration().getNodeID());
			watchService.stop();
		} catch (Exception e) {
			// ignore
			e.printStackTrace();
		}
		
		try {
			logger.debug("Logout user {} (node ̣{})", credentials.getUserId(), node.getNetworkConfiguration().getNodeID());
			logoutUser();
		} catch (NoPeerConnectionException | NoSessionException e) {
			// ignore
			e.printStackTrace();
		}
		
		
	}
}
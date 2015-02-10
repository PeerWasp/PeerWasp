package org.peerbox.client;

import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.TestExecutionUtil;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.mockito.Mockito;
import org.peerbox.app.config.IUserConfig;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.events.MessageBus;
import org.peerbox.watchservice.ActionExecutor;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.FolderWatchService;
import org.peerbox.watchservice.filetree.FileTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientNode {
	private static final Logger logger = LoggerFactory.getLogger(ClientNode.class);

	private IH2HNode node;
	private UserCredentials credentials;
	private Path rootPath;

	private FileTree fileTree;
	private FileEventManager fileEventManager;
	private IFileManager fileManager;
	private ActionExecutor actionExecutor;
	private FolderWatchService watchService;
	private MessageBus messageBus;

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

	public ActionExecutor getActionExecutor() {
		return actionExecutor;
	}

	public IFileManager getFileManager() {
		return fileManager;
	}

	private void initialization() throws Exception {
		// create path
		if (!Files.exists(rootPath)) {
			Files.createDirectory(rootPath);
		}

		messageBus = new MessageBus();
		// create managers and initialization
		INodeManager manager = Mockito.mock(INodeManager.class);
		Mockito.stub(manager.getNode()).toReturn(node);

		IUserConfig userConfig = Mockito.mock(IUserConfig.class);

		fileManager = new FileManager(manager, userConfig, messageBus);
		fileTree = new FileTree(rootPath, true);
		fileEventManager = new FileEventManager(fileTree, messageBus);
		actionExecutor = new ActionExecutor(fileEventManager, fileManager);
		watchService = new FolderWatchService();
		watchService.addFileEventListener(fileEventManager);

		// remote events
		node.getFileManager().subscribeFileEvents(fileEventManager);

		// login
		logger.debug("Login user {}", credentials.getUserId());
		loginUser();

		// start monitoring folder
		logger.debug("Start watchservice");
		watchService.start(rootPath);
		// start processing actions
		logger.debug("Start action executor");
		actionExecutor.start();
	}

	private void loginUser() throws NoPeerConnectionException {
		IFileAgent fileAgent = Mockito.mock(IFileAgent.class);
		Mockito.stub(fileAgent.getRoot()).toReturn(rootPath.toFile());
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
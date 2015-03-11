package org.peerbox.app;


import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.notifications.FileEventAggregator;
import org.peerbox.watchservice.ActionExecutor;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.FolderWatchService;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.persistency.LocalFileDao;
import org.peerbox.watchservice.filetree.persistency.PeriodicFileDataPersister;
import org.peerbox.watchservice.filetree.persistency.RemoteFileDao;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class ClientContext {

	private Injector clientInjector;

	private UserConfig userConfig;

	private INodeManager nodeManager;
	private IUserManager userManager;
	private IFileManager fileManager;

	private ActionExecutor actionExecutor;
	private FileEventManager fileEventManager;
	private FolderWatchService folderWatchService;
	private PeriodicFileDataPersister remoteProfilePersister;
	private FileEventAggregator fileEventAggregator;

	private FileTree fileTree;
	private LocalFileDao localFileDao;
	private RemoteFileDao remoteFileDao;

	public ClientContext() {

	}

	public Injector getInjector() {
		return clientInjector;
	}

	@Inject
	public void setInjector(Injector clientInjector) {
		this.clientInjector = clientInjector;
	}

	public UserConfig getUserConfig() {
		return userConfig;
	}

	@Inject
	public void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
	}

	public ActionExecutor getActionExecutor() {
		return actionExecutor;
	}

	@Inject
	public void setActionExecutor(ActionExecutor actionExecutor) {
		this.actionExecutor = actionExecutor;
	}

	public FileEventManager getFileEventManager() {
		return fileEventManager;
	}

	@Inject
	public void setFileEventManager(FileEventManager fileEventManager) {
		this.fileEventManager = fileEventManager;
	}

	public FolderWatchService getFolderWatchService() {
		return folderWatchService;
	}

	@Inject
	public void setFolderWatchService(FolderWatchService folderWatchService) {
		this.folderWatchService = folderWatchService;
	}

	public INodeManager getNodeManager() {
		return nodeManager;
	}

	@Inject
	public void setNodeManager(INodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}

	public IUserManager getUserManager() {
		return userManager;
	}

	@Inject
	public void setUserManager(IUserManager userManager) {
		this.userManager = userManager;
	}

	public IFileManager getFileManager() {
		return fileManager;
	}

	@Inject
	public void setFileManager(IFileManager fileManager) {
		this.fileManager = fileManager;
	}

	public PeriodicFileDataPersister getRemoteProfilePersister() {
		return remoteProfilePersister;
	}

	@Inject
	public void setRemoteProfilePersister(PeriodicFileDataPersister persister) {
		this.remoteProfilePersister = persister;
	}

	public FileEventAggregator getFileEventAggregator() {
		return fileEventAggregator;
	}

	@Inject
	public void setFileEventAggregator(FileEventAggregator fileEventAggregator) {
		this.fileEventAggregator = fileEventAggregator;
	}

	public LocalFileDao getLocalFileDao() {
		return localFileDao;
	}

	@Inject
	public void setFileDao(LocalFileDao fileDao) {
		this.localFileDao = fileDao;
	}

	public RemoteFileDao getRemoteFileDao() {
		return remoteFileDao;
	}

	@Inject
	public void setRemoteFileDao(RemoteFileDao remoteFileDao) {
		this.remoteFileDao = remoteFileDao;
	}

	public FileTree getFileTree() {
		return fileTree;
	}

	@Inject
	public void setFileTree(FileTree fileTree) {
		this.fileTree = fileTree;
	}



}

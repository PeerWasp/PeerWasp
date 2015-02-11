package org.peerbox.app;


import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.watchservice.ActionExecutor;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.FolderWatchService;
import org.peerbox.watchservice.filetree.persistency.H2HUserProfilePersister;

import com.google.inject.Singleton;

@Singleton
public class ClientContext {

	private ActionExecutor actionExecutor;
	private FileEventManager fileEventManager;
	private FolderWatchService folderWatchService;
	private INodeManager nodeManager;
	private H2HUserProfilePersister remoteProfilePersister;

	public ClientContext() {

	}

	public ActionExecutor getActionExecutor() {
		return actionExecutor;
	}

	public void setActionExecutor(ActionExecutor actionExecutor) {
		this.actionExecutor = actionExecutor;
	}

	public FileEventManager getFileEventManager() {
		return fileEventManager;
	}

	public void setFileEventManager(FileEventManager fileEventManager) {
		this.fileEventManager = fileEventManager;
	}

	public FolderWatchService getFolderWatchService() {
		return folderWatchService;
	}

	public void setFolderWatchService(FolderWatchService folderWatchService) {
		this.folderWatchService = folderWatchService;
	}

	public INodeManager getNodeManager() {
		return nodeManager;
	}

	public void setNodeManager(INodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}

	public H2HUserProfilePersister getRemoteProfilePersister() {
		return remoteProfilePersister;
	}

	public void setRemoteProfilePersister(H2HUserProfilePersister persister) {
		this.remoteProfilePersister = persister;
	}
}

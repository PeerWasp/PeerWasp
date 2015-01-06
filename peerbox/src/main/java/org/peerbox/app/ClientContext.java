package org.peerbox.app;


import org.peerbox.app.manager.node.NodeManager;
import org.peerbox.watchservice.ActionExecutor;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.FolderWatchService;

import com.google.inject.Singleton;

@Singleton
public class ClientContext {
	
	private ActionExecutor actionExecutor;
	private FileEventManager fileEventManager;
	private FolderWatchService folderWatchService;
	private NodeManager h2hManager;
	
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

	public NodeManager getH2hManager() {
		return h2hManager;
	}

	public void setH2hManager(NodeManager h2hManager) {
		this.h2hManager = h2hManager;
	}
}

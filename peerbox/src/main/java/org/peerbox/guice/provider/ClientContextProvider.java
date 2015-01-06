package org.peerbox.guice.provider;

import org.peerbox.app.ClientContext;
import org.peerbox.app.manager.node.NodeManager;
import org.peerbox.watchservice.ActionExecutor;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.FolderWatchService;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ClientContextProvider implements Provider<ClientContext> {
	
	@Inject
	private ActionExecutor actionExecutor;
	@Inject
	private FileEventManager fileEventManager;
	@Inject
	private FolderWatchService folderWatchService;
	@Inject
	private NodeManager h2hManager;
	
	@Override
	public ClientContext get() {
		ClientContext ctx = new ClientContext();
		ctx.setActionExecutor(actionExecutor);
		ctx.setFileEventManager(fileEventManager);
		ctx.setFolderWatchService(folderWatchService);
		ctx.setH2hManager(h2hManager);
		return ctx;
	}
}

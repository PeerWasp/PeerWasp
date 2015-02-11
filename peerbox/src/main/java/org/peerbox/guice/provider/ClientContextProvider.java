package org.peerbox.guice.provider;

import org.peerbox.app.ClientContext;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.watchservice.ActionExecutor;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.FolderWatchService;
import org.peerbox.watchservice.filetree.persistency.H2HUserProfilePersister;

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
	private INodeManager nodeManager;
	@Inject
	private H2HUserProfilePersister remoteProfilePersister;

	@Override
	public ClientContext get() {
		ClientContext ctx = new ClientContext();
		ctx.setActionExecutor(actionExecutor);
		ctx.setFileEventManager(fileEventManager);
		ctx.setFolderWatchService(folderWatchService);
		ctx.setNodeManager(nodeManager);
		ctx.setRemoteProfilePersister(remoteProfilePersister);
		return ctx;
	}
}

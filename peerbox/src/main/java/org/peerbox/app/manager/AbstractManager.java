package org.peerbox.app.manager;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.events.MessageBus;

import com.google.inject.Inject;

public class AbstractManager {

	private final INodeManager nodeManager;
	
	private final MessageBus messageBus;

	@Inject
	public AbstractManager(final INodeManager nodeManager, final MessageBus messageBus) {
		this.nodeManager = nodeManager;
		this.messageBus = messageBus;
	}

	protected final IFileManager getFileManager() {
		return nodeManager.getNode().getFileManager();
	}

	protected final IUserManager getUserManager() {
		return nodeManager.getNode().getUserManager();
	}
	
	protected final MessageBus getMessageBus() {
		return messageBus;
	}

}

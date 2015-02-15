package org.peerbox.app.manager;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.events.MessageBus;

public class AbstractManager {

	private final INodeManager nodeManager;
	private final MessageBus messageBus;

	public AbstractManager(final INodeManager nodeManager, final MessageBus messageBus) {
		this.nodeManager = nodeManager;
		this.messageBus = messageBus;
	}

	protected final IFileManager getH2HFileManager() {
		return nodeManager.getNode().getFileManager();
	}

	protected final IUserManager getH2HUserManager() {
		return nodeManager.getNode().getUserManager();
	}

	protected final MessageBus getMessageBus() {
		return messageBus;
	}

	protected final IFileConfiguration getFileConfiguration() {
		return nodeManager.getFileConfiguration();
	}

}

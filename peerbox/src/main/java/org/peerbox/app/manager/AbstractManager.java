package org.peerbox.app.manager;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.peerbox.IUserConfig;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.events.MessageBus;

public class AbstractManager {

	private final IUserConfig userConfig;
	private final INodeManager nodeManager;
	
	private final MessageBus messageBus;

	public AbstractManager(final INodeManager nodeManager, final IUserConfig userConfig, final MessageBus messageBus) {
		this.nodeManager = nodeManager;
		this.userConfig = userConfig;
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
	
	protected final IFileConfiguration getFileConfiguration() { 
		return nodeManager.getFileConfiguration();
	}
	
	protected final Path getRootPath() {
		return userConfig.getRootPath();
	}

}

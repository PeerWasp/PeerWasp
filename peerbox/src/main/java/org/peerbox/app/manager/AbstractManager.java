package org.peerbox.app.manager;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.peerbox.events.MessageBus;

import com.google.inject.Inject;

public class AbstractManager {

	private final IH2HManager h2hManager;
	
	private final MessageBus messageBus;

	@Inject
	public AbstractManager(final IH2HManager h2hManager, final MessageBus messageBus) {
		this.h2hManager = h2hManager;
		this.messageBus = messageBus;
	}

	protected final IFileManager getFileManager() {
		return h2hManager.getNode().getFileManager();
	}

	protected final IUserManager getUserManager() {
		return h2hManager.getNode().getUserManager();
	}
	
	protected final MessageBus getMessageBus() {
		return messageBus;
	}

}

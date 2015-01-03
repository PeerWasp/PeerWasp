package org.peerbox.app.manager;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IUserManager;

import com.google.inject.Inject;

public class AbstractManager {

	private final IH2HManager h2hManager;

	@Inject
	public AbstractManager(final IH2HManager h2hManager) {
		this.h2hManager = h2hManager;
	}

	protected final IFileManager getFileManager() {
		return h2hManager.getNode().getFileManager();
	}

	protected final IUserManager getUserManager() {
		return h2hManager.getNode().getUserManager();
	}

}

package org.peerbox.app.manager.file;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public final class RemoteFileDeletedMessage extends AbstractFileMessage {

	public RemoteFileDeletedMessage(FileHelper file) {
		super(file);
	}

}

package org.peerbox.app.manager.file;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public final class RemoteFileAddedMessage extends AbstractFileMessage {

	public RemoteFileAddedMessage(FileHelper file) {
		super(file);
	}

}

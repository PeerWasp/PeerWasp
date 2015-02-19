package org.peerbox.app.manager.file;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileMovedMessage extends AbstractFileMessage {

	public LocalFileMovedMessage(FileHelper file) {
		super(file);
	}

}

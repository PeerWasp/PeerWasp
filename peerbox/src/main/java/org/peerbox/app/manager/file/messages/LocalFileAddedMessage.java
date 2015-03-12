package org.peerbox.app.manager.file.messages;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileAddedMessage extends AbstractFileMessage{

	public LocalFileAddedMessage(FileHelper file) {
		super(file);

	}

}

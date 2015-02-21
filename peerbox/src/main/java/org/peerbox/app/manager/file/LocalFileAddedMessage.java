package org.peerbox.app.manager.file;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileAddedMessage extends AbstractFileMessage{

	public LocalFileAddedMessage(FileHelper file) {
		super(file);

	}

}

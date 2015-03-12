package org.peerbox.app.manager.file.messages;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileDeletedMessage extends AbstractFileMessage{

	public LocalFileDeletedMessage(FileHelper file) {
		super(file);
	}

}

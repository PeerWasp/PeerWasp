package org.peerbox.app.manager.file;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileDeletedMessage extends AbstractFileMessage{

	public LocalFileDeletedMessage(FileHelper file) {
		super(file);
	}

}

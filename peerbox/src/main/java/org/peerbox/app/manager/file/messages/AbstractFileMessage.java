package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.IFileMessage;
import org.peerbox.presenter.settings.synchronization.FileHelper;

class AbstractFileMessage implements IFileMessage {

	private final FileHelper file;

	public AbstractFileMessage(final FileHelper file) {
		this.file = file;
	}

	@Override
	public FileHelper getFile() {
		return file;
	}

}

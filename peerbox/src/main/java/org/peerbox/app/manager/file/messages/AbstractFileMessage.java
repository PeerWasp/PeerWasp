package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.IFileMessage;

class AbstractFileMessage implements IFileMessage {

	private final FileInfo file;

	public AbstractFileMessage(final FileInfo file) {
		this.file = file;
	}

	@Override
	public FileInfo getFile() {
		return file;
	}

}

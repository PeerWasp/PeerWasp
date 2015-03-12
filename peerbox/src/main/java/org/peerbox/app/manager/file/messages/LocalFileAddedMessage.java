package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class LocalFileAddedMessage extends AbstractFileMessage {

	public LocalFileAddedMessage(FileInfo file) {
		super(file);
	}

}

package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class LocalFileDeletedMessage extends AbstractFileMessage {

	public LocalFileDeletedMessage(FileInfo file) {
		super(file);
	}

}

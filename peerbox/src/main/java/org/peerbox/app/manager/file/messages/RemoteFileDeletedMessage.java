package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class RemoteFileDeletedMessage extends AbstractFileMessage {

	public RemoteFileDeletedMessage(FileInfo file) {
		super(file);
	}

}

package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class RemoteFileAddedMessage extends AbstractFileMessage {

	public RemoteFileAddedMessage(FileInfo file) {
		super(file);
	}
}

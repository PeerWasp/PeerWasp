package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class RemoteFileUpdatedMessage extends AbstractFileMessage {

	public RemoteFileUpdatedMessage(FileInfo file) {
		super(file);
	}

}

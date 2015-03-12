package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class LocalFileUpdatedMessage extends AbstractFileMessage {

	public LocalFileUpdatedMessage(FileInfo file) {
		super(file);
	}

}

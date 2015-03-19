package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class LocalFileSoftDeleteMessage extends AbstractFileMessage {

	public LocalFileSoftDeleteMessage(final FileInfo file) {
		super(file);
	}

}

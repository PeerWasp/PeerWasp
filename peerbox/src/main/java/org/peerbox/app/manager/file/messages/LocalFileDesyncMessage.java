package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class LocalFileDesyncMessage extends AbstractFileMessage {

	public LocalFileDesyncMessage(final FileInfo file) {
		super(file);
	}

}

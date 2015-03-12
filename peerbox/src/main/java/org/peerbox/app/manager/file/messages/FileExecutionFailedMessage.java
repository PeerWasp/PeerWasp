package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class FileExecutionFailedMessage extends AbstractFileMessage {

	public FileExecutionFailedMessage(final FileInfo file) {
		super(file);
	}

}

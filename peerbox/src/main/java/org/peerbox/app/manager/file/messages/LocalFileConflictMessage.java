package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class LocalFileConflictMessage extends AbstractFileMessage {

	public LocalFileConflictMessage(FileInfo file) {
		super(file);
	}

}

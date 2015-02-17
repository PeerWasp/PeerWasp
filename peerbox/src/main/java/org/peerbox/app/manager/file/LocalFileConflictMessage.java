package org.peerbox.app.manager.file;

import java.nio.file.Path;

public final class LocalFileConflictMessage extends AbstractFileMessage {

	public LocalFileConflictMessage(Path path) {
		super(path);
	}

}

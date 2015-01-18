package org.peerbox.app.manager.file;

import java.nio.file.Path;

public final class FileConflictMessage extends AbstractFileMessage {

	FileConflictMessage(Path path) {
		super(path);
	}

}

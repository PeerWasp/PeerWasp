package org.peerbox.app.manager.file;

import java.nio.file.Path;

public final class FileDeleteMessage extends AbstractFileMessage {

	FileDeleteMessage(Path path) {
		super(path);
	}

}

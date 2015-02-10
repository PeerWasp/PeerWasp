package org.peerbox.app.manager.file;

import java.nio.file.Path;

public final class FileDeleteMessage extends AbstractFileMessage {

	public FileDeleteMessage(Path path) {
		super(path);
	}

}

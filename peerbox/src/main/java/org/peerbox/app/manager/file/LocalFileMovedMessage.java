package org.peerbox.app.manager.file;

import java.nio.file.Path;

public class LocalFileMovedMessage extends AbstractFileMessage {

	public LocalFileMovedMessage(Path path) {
		super(path);
	}

}

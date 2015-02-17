package org.peerbox.app.manager.file;

import java.nio.file.Path;

public class LocalFileAddedMessage extends AbstractFileMessage{

	public LocalFileAddedMessage(Path path) {
		super(path);

	}

}

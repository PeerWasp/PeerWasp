package org.peerbox.app.manager.file;

import java.nio.file.Path;

public class LocalFileDeletedMessage extends AbstractFileMessage{

	public LocalFileDeletedMessage(Path path) {
		super(path);
	}

}

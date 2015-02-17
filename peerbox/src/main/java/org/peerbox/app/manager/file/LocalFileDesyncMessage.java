package org.peerbox.app.manager.file;

import java.nio.file.Path;

public class LocalFileDesyncMessage extends AbstractFileMessage{

	public LocalFileDesyncMessage(Path path) {
		super(path);
	}

}

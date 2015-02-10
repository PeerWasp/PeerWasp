package org.peerbox.app.manager.file;

import java.nio.file.Path;

public class FileDesyncMessage extends AbstractFileMessage{

	public FileDesyncMessage(Path path) {
		super(path);
	}

}

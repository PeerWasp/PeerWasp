package org.peerbox.app.manager.file;

import java.nio.file.Path;

public class FileExecutionFailedMessage extends AbstractFileMessage{

	public FileExecutionFailedMessage(Path path) {
		super(path);
	}

}

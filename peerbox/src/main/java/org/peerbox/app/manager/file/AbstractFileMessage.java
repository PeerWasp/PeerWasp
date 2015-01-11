package org.peerbox.app.manager.file;

import java.nio.file.Path;

class AbstractFileMessage implements IFileMessage {

	private final Path path;

	AbstractFileMessage(final Path path) {
		this.path = path;
	}

	public Path getPath() {
		return path;
	}

}

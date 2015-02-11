package org.peerbox.app.manager.file;

import java.nio.file.Path;

public class AbstractFileMessage implements IFileMessage {

	private final Path path;

	public AbstractFileMessage(final Path path) {
		this.path = path;
	}

	@Override
	public Path getPath() {
		return path;
	}

}

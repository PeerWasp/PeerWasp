package org.peerbox.server.servlets.messages;

import java.nio.file.Path;

/**
 * Sharing a folder - we expect exactly 1 path to a folder as argument
 *
 * @author albrecht
 *
 */
public class ShareMessage {
	private Path path;

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}
}

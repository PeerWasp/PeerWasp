package org.peerbox.server.servlets.messages;

import java.nio.file.Path;

/**
 * Versions of file - we expect exactly 1 path to a file as argument
 * 
 * @author albrecht
 *
 */
public class FileRecoveryMessage {
	private Path path;
	
	public Path getPath() {
		return path;
	}
	
	public void setPath(Path path) {
		this.path = path;
	}
}

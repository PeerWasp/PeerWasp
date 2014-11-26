package org.peerbox.server.servlets.messages;

import java.nio.file.Path;
import java.util.List;

/**
 * Delete - we expect 1 or more files or folders as argument
 * 
 * @author albrecht
 *
 */
public class DeleteMessage {
	private List<Path> paths;

	public List<Path> getPaths() {
		return paths;
	}
}

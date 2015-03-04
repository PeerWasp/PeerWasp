package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;

/**
 * This class is used to represent files and folders as simple
 * as possible using only two properties: The object's Path and a 
 * boolean denoting if the object is a file or a folder. It is 
 * primarily used to transfer this information between PeerWasp as
 * part of messages sent between components over the {@link org.
 * peerbox.events.MessageBus MessageBus}.
 * @author Claudio
 *
 */
public class FileHelper {

	private Path path;
	private boolean isFile;

	public FileHelper(Path path, boolean isFile) {
		this.path = path;
		this.isFile = isFile;
	}

	public Path getPath() {
		return path;
	}

	public boolean isFile() {
		return isFile;
	}

	public boolean isFolder() {
		return !isFile;
	}
}

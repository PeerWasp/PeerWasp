package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;

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

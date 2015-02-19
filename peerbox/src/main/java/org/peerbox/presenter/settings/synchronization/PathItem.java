package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;

public class PathItem{
	private Path path;
	private boolean isFile;

	public PathItem(Path path) {
		this(path, true);
	}

	public PathItem(Path path, boolean isFile) {
		this.path = path;
		setIsFile(isFile);
	}

	public Path getPath() {
		return path;
	}

	public boolean isFile() {
		return isFile;
	}

	private void setIsFile(boolean isFile) {
		this.isFile = isFile;
	}

	public boolean isFolder() {
		return !isFile;
	}

	@Override
	public String toString() {
		return "";
	}
}
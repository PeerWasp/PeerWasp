package org.peerbox.forcesync;

import java.nio.file.Path;

import org.peerbox.watchservice.filetree.composite.FileComponent;

public class FileInfo {
	private Path path;
	private boolean isFolder;
	private String hash;

	public FileInfo(Path path, boolean isFolder) {
		this.path = path;
		this.hash = null;
		this.isFolder = isFolder;
	}

	public FileInfo(FileComponent component) {
		this.path = component.getPath();
		this.hash = component.getContentHash();
		this.isFolder = component.isFolder();
	}

	public Path getPath() {
		return path;
	}

	public String getHash() {
		return hash;
	}

	public FileInfo setHash(String hash) {
		this.hash = hash;
		return this;
	}

	public boolean isFile() {
		return !isFolder();
	}

	public boolean isFolder() {
		return isFolder;
	}
}

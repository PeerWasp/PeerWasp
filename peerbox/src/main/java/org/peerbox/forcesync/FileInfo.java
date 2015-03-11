package org.peerbox.forcesync;

import java.nio.file.Path;

import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.watchservice.PathUtils;
import org.peerbox.watchservice.filetree.composite.FileComponent;

/**
 * This class is used to represent files and folders as simple
 * as possible using a limited set of properties. It is
 * primarily used to transfer this information in PeerWasp as
 * part of messages sent between components, e.g. over the {@link org.
 * peerbox.events.MessageBus MessageBus}.
 * It is further used as a wrapper for different interfaces (H2H interface
 * and PeerWasp interface).
 *
 */
public class FileInfo {

	private Path path;
	private boolean isFolder;
	private String contentHash;

	/**
	 * Creates a new FileInfo instance with hash set to empty string.
	 *
	 * @param path
	 * @param isFolder
	 */
	public FileInfo(Path path, boolean isFolder) {
		this.path = path;
		this.contentHash = "";
		this.isFolder = isFolder;
	}

	/**
	 * Creates a new FileInfo instance with hash set.
	 *
	 * @param path
	 * @param isFolder
	 * @param hash the content hash
	 */
	public FileInfo(Path path, boolean isFolder, String hash) {
		this.path = path;
		this.contentHash = hash;
		this.isFolder = isFolder;
	}

	/**
	 * Creates a new FileInfo instance given a FileComponent.
	 * All attributes are copied from the component.
	 *
	 * @param component
	 */
	public FileInfo(FileComponent component) {
		this.path = component.getPath();
		this.contentHash = component.getContentHash();
		this.isFolder = component.isFolder();
	}

	/**
	 * Creates a new FileInfo instance given a FileNode.
	 * All attributes are copied from the node.
	 *
	 * @param node
	 */
	public FileInfo(FileNode node) {
		this(node.getFile().toPath(), node.isFolder());
		// FileNode hash is null for folders!
		if (node.isFile()) {
			contentHash = (node.getMd5() != null) ? PathUtils.base64Encode(node.getMd5()) : "";
		}
	}

	public Path getPath() {
		return path;
	}

	public String getContentHash() {
		return contentHash;
	}

	public void setContentHash(String contentHash) {
		this.contentHash = contentHash;
	}

	public boolean isFile() {
		return !isFolder();
	}

	public boolean isFolder() {
		return isFolder;
	}
}

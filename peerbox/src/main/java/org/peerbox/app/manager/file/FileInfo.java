package org.peerbox.app.manager.file;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;
import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.presenter.settings.synchronization.PathItem;
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
public class FileInfo implements Comparable<FileInfo> {

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
		this(path, isFolder, "");
	}

	/**
	 * Creates a new FileInfo instance with hash set.
	 *
	 * @param path
	 * @param isFolder
	 * @param contentHash the content hash
	 */
	public FileInfo(Path path, boolean isFolder, String contentHash) {
		this.path = path;
		this.isFolder = isFolder;
		this.contentHash = contentHash;
	}

	/**
	 * Creates a new FileInfo instance given a FileComponent.
	 * All attributes are copied from the component.
	 *
	 * @param component
	 */
	public FileInfo(FileComponent component) {
		this(component.getPath(), component.isFolder(), component.getContentHash());
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
		if (node.isFile() && node.getMd5() != null) {
			contentHash = PathUtils.base64Encode(node.getMd5());
		}
	}

	/**
	 * Creates a new FileInfo instance given a FileEvent.
	 * All attributes are copied from the node.
	 *
	 * @param fileEvent
	 */
	public FileInfo(IFileEvent fileEvent) {
		this(fileEvent.getFile().toPath(), fileEvent.isFolder());
	}

	/**
	 * Creates a new FileInfo instance given a PathItem.
	 * All attributes are copied from the item.
	 *
	 * @param pathItem
	 */
	public FileInfo(PathItem pathItem) {
		this(pathItem.getPath(), pathItem.isFolder());
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

	/**
	 * Comparable interface implementation
	 */
	@Override
	public int compareTo(FileInfo o) {
		return this.getPath().compareTo(o.getPath());
	}
}

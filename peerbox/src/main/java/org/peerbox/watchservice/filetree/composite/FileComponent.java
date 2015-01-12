package org.peerbox.watchservice.filetree.composite;

import java.nio.file.Path;
import java.util.Set;

import org.peerbox.watchservice.IAction;

public interface FileComponent {

	FileComponent getComponent(String path);

	void putComponent(String path, FileComponent component);

	FileComponent deleteComponent(String path);

	IAction getAction();

	Path getPath();

	void setPath(Path path);

	FolderComposite getParent();

	void setParent(FolderComposite parent);

	boolean isFile();

	boolean isFolder();

	String getContentHash();

	boolean bubbleContentHashUpdate();

	String getStructureHash();

	void setStructureHash(String hash);

	boolean isSynchronized();

	void setIsSynchronized(boolean isSynchronized);

	boolean isUploaded();

	void setIsUploaded(boolean isUploaded);

	boolean isReady();

	void getSynchronizedChildrenPaths(Set<Path> synchronizedPaths);

}

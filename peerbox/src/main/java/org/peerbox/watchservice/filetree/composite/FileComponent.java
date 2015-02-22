package org.peerbox.watchservice.filetree.composite;

import java.nio.file.Path;
import java.util.Set;

import org.peerbox.watchservice.IAction;

public interface FileComponent {

	long getId();

	void setId(long id);

	FileComponent getComponent(Path path);

	void putComponent(Path path, FileComponent component);

	FileComponent deleteComponent(Path path);

	IAction getAction();

	Path getPath();

	void setPath(Path path);

	FolderComposite getParent();

	void setParent(FolderComposite parent);

	boolean isFile();

	boolean isFolder();

	String getContentHash();

	void setContentHash(String contentHash);

	boolean updateContentHash();

	String getStructureHash();

	void setStructureHash(String hash);

	boolean isSynchronized();

	void setIsSynchronized(boolean isSynchronized);

	boolean isUploaded();

	void setIsUploaded(boolean isUploaded);

	boolean isReady();

	void getSynchronizedChildrenPaths(Set<Path> synchronizedPaths);

	void updateStateOnLocalDelete();

	@Override
	String toString();

}

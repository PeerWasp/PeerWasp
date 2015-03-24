package org.peerbox.watchservice.filetree.composite;

import java.nio.file.Path;

import org.peerbox.watchservice.IAction;

public interface FileComponent {

	IAction getAction();

	Path getPath();

	void setPath(Path path);

	FolderComposite getParent();

	void setParent(FolderComposite parent);

	boolean isFile();

	boolean isFolder();

	String getContentHash();

	boolean updateContentHash();

	boolean isSynchronized();

	void setIsSynchronized(boolean isSynchronized);

	boolean isUploaded();

	void setIsUploaded(boolean isUploaded);

	boolean isReady();

	@Override
	String toString();
}

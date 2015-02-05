package org.peerbox.watchservice;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.peerbox.watchservice.filetree.IFileTree;

public interface IFileEventManager {

	public IFileTree getFileTree();
	public ActionQueue getFileComponentQueue();

	public void onFileDesynchronized(Path path);
	public void onFileSynchronized(Path path, boolean isFolder);
	public void onFileAdd(IFileAddEvent fileEvent);
	public void onLocalFileHardDelete(Path toDelete);

}

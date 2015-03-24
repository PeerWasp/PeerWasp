package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.Set;

import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.peerbox.events.MessageBus;
import org.peerbox.watchservice.filetree.IFileTree;

public interface IFileEventManager {

	public IFileTree getFileTree();
	public FileComponentQueue getFileComponentQueue();

	public void onFileSoftDeleted(Path path);
	public void onFileSynchronized(Path path, boolean isFolder);
	public void onFileAdd(IFileAddEvent fileEvent);
	public void onLocalFileHardDelete(Path toDelete);

	public MessageBus getMessageBus();
	public Set<Path> getFailedOperations();
	public void initiateForceSync(Path topLevel);
}

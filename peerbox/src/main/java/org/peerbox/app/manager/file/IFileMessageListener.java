package org.peerbox.app.manager.file;

import org.peerbox.app.activity.ActivityItem;
import org.peerbox.events.IMessageListener;

import net.engio.mbassy.listener.Handler;

public interface IFileMessageListener extends IMessageListener {

	@Handler
	void onLocalFileAdded(LocalFileAddedMessage message);

	@Handler
	void onLocalFileMoved(LocalFileMovedMessage message);

	@Handler
	void onLocalFileDeleted(LocalFileDeletedMessage message);
	
	@Handler
	void onLocalFileUpdated(LocalFileUpdatedMessage message);

	@Handler
	void onLocalFileConfilct(LocalFileConflictMessage message);
	
	@Handler
	void onLocalFileDesynchronized(LocalFileDesyncMessage message);
	
	@Handler
	void onRemoteFileAdded(RemoteFileAddedMessage message);

	@Handler
	void onRemoteFileMoved(RemoteFileMovedMessage message);

	@Handler
	void onRemoteFileDeleted(RemoteFileDeletedMessage message);
	
	@Handler
	void onRemoteFileUpdated(RemoteFileUpdatedMessage message);
	
	@Handler
	void onFileExecutionFailed(FileExecutionFailedMessage message);

}

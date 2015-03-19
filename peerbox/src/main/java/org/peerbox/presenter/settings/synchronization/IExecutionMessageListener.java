package org.peerbox.presenter.settings.synchronization;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.manager.file.messages.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.messages.FileExecutionStartedMessage;
import org.peerbox.app.manager.file.messages.FileExecutionSucceededMessage;
import org.peerbox.app.manager.file.messages.LocalFileSoftDeleteMessage;
import org.peerbox.app.manager.file.messages.LocalShareFolderMessage;
import org.peerbox.app.manager.file.messages.RemoteFileDeletedMessage;
import org.peerbox.app.manager.file.messages.RemoteFileMovedMessage;
import org.peerbox.app.manager.file.messages.RemoteShareFolderMessage;
import org.peerbox.events.IMessageListener;

public interface IExecutionMessageListener extends IMessageListener {

	@Handler
	void onExecutionStarts(FileExecutionStartedMessage message);
	
	@Handler
	void onExecutionSucceeds(FileExecutionSucceededMessage message);
	
	@Handler
	void onExecutionFails(FileExecutionFailedMessage message);
	
	@Handler
	void onFileSoftDeleted(LocalFileSoftDeleteMessage message);
	
	@Handler
	void onFileRemotelyDeleted(RemoteFileDeletedMessage message);
	
	@Handler
	void onFileRemotelyMoved(RemoteFileMovedMessage message);
	
	@Handler
	void onRemoteFolderShared(RemoteShareFolderMessage message);
	
	@Handler
	void onLocalFolderShared(LocalShareFolderMessage message);
}

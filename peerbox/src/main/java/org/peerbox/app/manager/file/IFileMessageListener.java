package org.peerbox.app.manager.file;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.manager.file.messages.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.messages.LocalFileAddedMessage;
import org.peerbox.app.manager.file.messages.LocalFileConflictMessage;
import org.peerbox.app.manager.file.messages.LocalFileDeletedMessage;
import org.peerbox.app.manager.file.messages.LocalFileDesyncMessage;
import org.peerbox.app.manager.file.messages.LocalFileMovedMessage;
import org.peerbox.app.manager.file.messages.LocalFileUpdatedMessage;
import org.peerbox.app.manager.file.messages.LocalShareFolderMessage;
import org.peerbox.app.manager.file.messages.RemoteFileAddedMessage;
import org.peerbox.app.manager.file.messages.RemoteFileDeletedMessage;
import org.peerbox.app.manager.file.messages.RemoteFileMovedMessage;
import org.peerbox.app.manager.file.messages.RemoteFileUpdatedMessage;
import org.peerbox.app.manager.file.messages.RemoteShareFolderMessage;
import org.peerbox.events.IMessageListener;

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

	@Handler
	void onRemoteShareFolder(RemoteShareFolderMessage message);

	@Handler
	void onLocalShareFolder(LocalShareFolderMessage message);

}

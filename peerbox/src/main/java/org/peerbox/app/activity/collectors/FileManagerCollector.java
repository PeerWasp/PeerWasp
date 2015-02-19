package org.peerbox.app.activity.collectors;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.activity.ActivityItem;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.activity.ActivityType;
import org.peerbox.app.manager.file.LocalFileAddedMessage;
import org.peerbox.app.manager.file.LocalFileConflictMessage;
import org.peerbox.app.manager.file.LocalFileDeletedMessage;
import org.peerbox.app.manager.file.LocalFileMovedMessage;
import org.peerbox.app.manager.file.LocalFileUpdatedMessage;
import org.peerbox.app.manager.file.RemoteFileDeletedMessage;
import org.peerbox.app.manager.file.LocalFileDesyncMessage;
import org.peerbox.app.manager.file.RemoteFileMovedMessage;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.RemoteFileAddedMessage;
import org.peerbox.app.manager.file.IFileMessage;
import org.peerbox.app.manager.file.IFileMessageListener;
import org.peerbox.app.manager.file.RemoteFileUpdatedMessage;
import org.peerbox.exceptions.NotImplException;

import com.google.inject.Inject;

class FileManagerCollector extends AbstractActivityCollector implements IFileMessageListener {

	@Inject
	protected FileManagerCollector(ActivityLogger activityLogger) {
		super(activityLogger);
	}

	@Handler
	@Override
	public void onLocalFileAdded(LocalFileAddedMessage upload) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Local add finished.")
				.setDescription(formatDescription(upload));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onLocalFileMoved(LocalFileMovedMessage download) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Local move finished.")
				.setDescription(formatDescription(download));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onLocalFileDeleted(LocalFileDeletedMessage delete) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Local delete finished.")
				.setDescription(formatDescription(delete));
		getActivityLogger().addActivityItem(item);
	}
	
	@Handler
	@Override
	public void onLocalFileUpdated(LocalFileUpdatedMessage message) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Local update finished.")
				.setDescription(formatDescription(message));
		getActivityLogger().addActivityItem(item);
	}
	
	@Handler
	@Override
	public void onLocalFileDesynchronized(LocalFileDesyncMessage desync){
		ActivityItem item = ActivityItem.create()
				.setTitle("Soft-delete finished. File has been locally deleted.")
				.setDescription(formatDescription(desync));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onLocalFileConfilct(LocalFileConflictMessage conflict) {
		ActivityItem item = ActivityItem.create()
				.setType(ActivityType.WARNING)
				.setTitle("Conflict detected, local version renamed.")
				.setDescription(formatDescription(conflict));
		getActivityLogger().addActivityItem(item);
	}
	
	@Handler
	@Override
	public void onRemoteFileAdded(RemoteFileAddedMessage upload) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Remote add finished.")
				.setDescription(formatDescription(upload));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onRemoteFileMoved(RemoteFileMovedMessage download) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Remote move finished.")
				.setDescription(formatDescription(download));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onRemoteFileDeleted(RemoteFileDeletedMessage delete) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Remote delete finished.")
				.setDescription(formatDescription(delete));
		getActivityLogger().addActivityItem(item);
	}
	
	@Handler
	@Override
	public void onRemoteFileUpdated(RemoteFileUpdatedMessage message) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Remote update finished.")
				.setDescription(formatDescription(message));
		getActivityLogger().addActivityItem(item);
	}
	
	@Handler
	@Override
	public void onFileExecutionFailed(FileExecutionFailedMessage failure) {
		ActivityItem item = ActivityItem.create()
				.setType(ActivityType.ERROR)
				.setTitle("All attempts to handle events failed.")
				.setDescription(formatDescription(failure));
		getActivityLogger().addActivityItem(item);
	}

	private String formatDescription(IFileMessage msg) {
		return String.format("%s", msg.getFile().getPath());
	}


}

package org.peerbox.app.activity.collectors;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.activity.ActivityItem;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.activity.ActivityType;
import org.peerbox.app.manager.file.FileConflictMessage;
import org.peerbox.app.manager.file.FileDeleteMessage;
import org.peerbox.app.manager.file.FileDesyncMessage;
import org.peerbox.app.manager.file.FileDownloadMessage;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.FileUploadMessage;
import org.peerbox.app.manager.file.IFileMessage;
import org.peerbox.app.manager.file.IFileMessageListener;
import org.peerbox.exceptions.NotImplException;

import com.google.inject.Inject;

class FileManagerCollector extends AbstractActivityCollector implements IFileMessageListener {

	@Inject
	protected FileManagerCollector(ActivityLogger activityLogger) {
		super(activityLogger);
	}

	@Handler
	@Override
	public void onFileUploaded(FileUploadMessage upload) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Upload finished.")
				.setDescription(formatDescription(upload));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onFileDownloaded(FileDownloadMessage download) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Download finished.")
				.setDescription(formatDescription(download));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onFileDeleted(FileDeleteMessage delete) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Delete finished.")
				.setDescription(formatDescription(delete));
		getActivityLogger().addActivityItem(item);
	}
	
	@Handler
	@Override
	public void onFileDesynchronized(FileDesyncMessage desync){
		ActivityItem item = ActivityItem.create()
				.setTitle("Soft-delete finished. File has been locally deleted.")
				.setDescription(formatDescription(desync));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onFileConfilct(FileConflictMessage conflict) {
		ActivityItem item = ActivityItem.create()
				.setType(ActivityType.WARNING)
				.setTitle("Conflict detected, local version renamed.")
				.setDescription(formatDescription(conflict));
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
		return String.format("%s", msg.getPath());
	}

}

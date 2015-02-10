package org.peerbox.app.manager.file;

import org.peerbox.events.IMessageListener;

import net.engio.mbassy.listener.Handler;

public interface IFileMessageListener extends IMessageListener {

	@Handler
	void onFileUploaded(FileUploadMessage upload);

	@Handler
	void onFileDownloaded(FileDownloadMessage download);

	@Handler
	void onFileDeleted(FileDeleteMessage delete);

	@Handler
	void onFileConfilct(FileConflictMessage conflict);
	
	@Handler
	void onFileDesynchronized(FileDesyncMessage desync);

}

package org.peerbox.app.manager.file;

import net.engio.mbassy.listener.Handler;

public interface IFileMessageListener {

	@Handler
	void onFileUploaded(FileUploadMessage upload);

	@Handler
	void onFileDownloaded(FileDownloadMessage download);

	@Handler
	void onFileDeleted(FileDeleteMessage download);

}

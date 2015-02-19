package org.peerbox.app.manager.file;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class FileExecutionFailedMessage extends AbstractFileMessage{

	public FileExecutionFailedMessage(final FileHelper file) {
		super(file);
	}

}

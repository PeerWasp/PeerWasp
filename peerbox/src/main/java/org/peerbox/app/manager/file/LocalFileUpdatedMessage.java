package org.peerbox.app.manager.file;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileUpdatedMessage extends AbstractFileMessage {

	public LocalFileUpdatedMessage(FileHelper file) {
		super(file);
	}

}

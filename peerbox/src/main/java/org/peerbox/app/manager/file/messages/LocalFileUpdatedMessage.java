package org.peerbox.app.manager.file.messages;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileUpdatedMessage extends AbstractFileMessage {

	public LocalFileUpdatedMessage(FileHelper file) {
		super(file);
	}

}

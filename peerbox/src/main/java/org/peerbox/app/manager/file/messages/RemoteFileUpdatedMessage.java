package org.peerbox.app.manager.file.messages;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class RemoteFileUpdatedMessage extends AbstractFileMessage {

	public RemoteFileUpdatedMessage(FileHelper file) {
		super(file);
	}

}

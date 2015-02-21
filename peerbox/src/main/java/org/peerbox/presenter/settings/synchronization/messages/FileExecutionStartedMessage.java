package org.peerbox.presenter.settings.synchronization.messages;

import org.peerbox.app.manager.file.AbstractFileMessage;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class FileExecutionStartedMessage extends AbstractFileMessage {

	public FileExecutionStartedMessage(final FileHelper file) {
		super(file);
	}

}

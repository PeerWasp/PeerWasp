package org.peerbox.presenter.settings.synchronization.messages;

import java.nio.file.Path;

import org.peerbox.app.manager.file.AbstractFileMessage;

public class FileExecutionStartedMessage extends AbstractFileMessage {

	public FileExecutionStartedMessage(Path path) {
		super(path);
	}

}

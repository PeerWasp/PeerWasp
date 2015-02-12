package org.peerbox.presenter.settings.synchronization.messages;

import java.nio.file.Path;

import org.peerbox.app.manager.file.AbstractFileMessage;

public class ExecutionStartsMessage extends AbstractFileMessage {

	public ExecutionStartsMessage(Path path) {
		super(path);
	}

}

package org.peerbox.presenter.settings.synchronization.messages;

import java.nio.file.Path;

import org.peerbox.app.manager.file.AbstractFileMessage;

public class ExecutionSuccessfulMessage extends AbstractFileMessage{

	public ExecutionSuccessfulMessage(Path path) {
		super(path);
	}
}

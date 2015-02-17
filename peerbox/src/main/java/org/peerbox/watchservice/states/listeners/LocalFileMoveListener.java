package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.LocalFileMovedMessage;
import org.peerbox.events.MessageBus;

public class LocalFileMoveListener extends FileOperationListener{
	public LocalFileMoveListener(final Path path, final MessageBus messageBus) {
		super(path, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileMove(getPath());
	}
	
	private void notifyLocalFileMove(final Path path) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileMovedMessage(path));
		}
	}

}


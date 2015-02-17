package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.LocalFileAddedMessage;
import org.peerbox.events.MessageBus;

public class LocalFileAddListener extends FileOperationListener{

	public LocalFileAddListener(Path path, MessageBus messageBus) {
		super(path, messageBus);
	}
	
	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileAdd(getPath());
	}
	

	private void notifyLocalFileAdd(final Path path) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileAddedMessage(path));
		}
	}
}

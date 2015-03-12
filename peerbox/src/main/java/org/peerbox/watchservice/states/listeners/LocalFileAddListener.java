package org.peerbox.watchservice.states.listeners;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.messages.LocalFileAddedMessage;
import org.peerbox.events.MessageBus;

public class LocalFileAddListener extends FileOperationListener {

	public LocalFileAddListener(FileInfo file, MessageBus messageBus) {
		super(file, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileAdd(getFile());
	}

	private void notifyLocalFileAdd(final FileInfo file) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileAddedMessage(file));
		}
	}

}

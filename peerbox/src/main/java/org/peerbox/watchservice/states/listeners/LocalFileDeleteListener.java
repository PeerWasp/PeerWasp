package org.peerbox.watchservice.states.listeners;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.messages.LocalFileDeletedMessage;
import org.peerbox.events.MessageBus;

public class LocalFileDeleteListener extends FileOperationListener {

	public LocalFileDeleteListener(final FileInfo file, final MessageBus messageBus) {
		super(file, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileDelete(getFile());
	}

	private void notifyLocalFileDelete(final FileInfo file) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileDeletedMessage(file));
		}
	}

}

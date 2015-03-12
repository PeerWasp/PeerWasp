package org.peerbox.watchservice.states.listeners;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.messages.LocalFileUpdatedMessage;
import org.peerbox.events.MessageBus;

public class LocalFileUpdateListener extends FileOperationListener {

	public LocalFileUpdateListener(final FileInfo file, MessageBus messageBus) {
		super(file, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileUpdate(getFile());
	}

	private void notifyLocalFileUpdate(FileInfo file) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileUpdatedMessage(file));
		}
	}

}
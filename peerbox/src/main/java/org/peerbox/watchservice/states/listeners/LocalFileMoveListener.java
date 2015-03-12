package org.peerbox.watchservice.states.listeners;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.messages.LocalFileMovedMessage;
import org.peerbox.events.MessageBus;

public class LocalFileMoveListener extends FileOperationListener {

	private final FileInfo source;

	public LocalFileMoveListener(final FileInfo source, final FileInfo dest, final MessageBus messageBus) {
		super(dest, messageBus);
		this.source = source;
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileMove(getFile());
	}

	private void notifyLocalFileMove(final FileInfo file) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileMovedMessage(source, file));
		}
	}

}

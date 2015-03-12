package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.messages.LocalFileMovedMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileMoveListener extends FileOperationListener{
	FileHelper source;
	public LocalFileMoveListener(final FileHelper source, final FileHelper dest, final MessageBus messageBus) {
		super(dest, messageBus);
		this.source = source;
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileMove(getFile());
	}
	
	private void notifyLocalFileMove(final FileHelper file) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileMovedMessage(source, file));
		}
	}
}


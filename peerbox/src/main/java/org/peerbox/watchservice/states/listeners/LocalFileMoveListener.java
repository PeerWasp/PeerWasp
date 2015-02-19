package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.LocalFileMovedMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileMoveListener extends FileOperationListener{
	public LocalFileMoveListener(final FileHelper file, final MessageBus messageBus) {
		super(file, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileMove(getFile());
	}
	
	private void notifyLocalFileMove(final FileHelper file) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileMovedMessage(file));
		}
	}

}


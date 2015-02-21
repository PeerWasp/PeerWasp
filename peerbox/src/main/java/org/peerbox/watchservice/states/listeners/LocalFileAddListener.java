package org.peerbox.watchservice.states.listeners;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.LocalFileAddedMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileAddListener extends FileOperationListener{

	public LocalFileAddListener(FileHelper file, MessageBus messageBus) {
		super(file, messageBus);
	}
	
	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileAdd(getFile());
	}
	

	private void notifyLocalFileAdd(final FileHelper file) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileAddedMessage(file));
		}
	}
}

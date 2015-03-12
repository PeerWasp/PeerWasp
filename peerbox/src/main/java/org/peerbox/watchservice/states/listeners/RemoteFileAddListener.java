package org.peerbox.watchservice.states.listeners;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.messages.LocalFileDeletedMessage;
import org.peerbox.app.manager.file.messages.RemoteFileAddedMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class RemoteFileAddListener extends FileOperationListener{

	public RemoteFileAddListener(final FileHelper file, MessageBus messageBus) {
		super(file, messageBus);
	}
	
	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyRemoteFileAdd(getFile());
	}
	
	private void notifyRemoteFileAdd(final FileHelper file){
		if (getMessageBus() != null) {
			getMessageBus().publish(new RemoteFileAddedMessage(file));
		}
	}
}

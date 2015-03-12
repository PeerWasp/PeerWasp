package org.peerbox.watchservice.states.listeners;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.messages.RemoteFileAddedMessage;
import org.peerbox.events.MessageBus;

public class RemoteFileAddListener extends FileOperationListener{

	public RemoteFileAddListener(final FileInfo file, MessageBus messageBus) {
		super(file, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyRemoteFileAdd(getFile());
	}

	private void notifyRemoteFileAdd(final FileInfo file){
		if (getMessageBus() != null) {
			getMessageBus().publish(new RemoteFileAddedMessage(file));
		}
	}

}

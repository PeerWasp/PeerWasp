package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.LocalFileDeletedMessage;
import org.peerbox.app.manager.file.RemoteFileAddedMessage;
import org.peerbox.events.MessageBus;

public class RemoteFileAddListener extends FileOperationListener{

	public RemoteFileAddListener(Path path, MessageBus messageBus) {
		super(path, messageBus);
	}
	
	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyRemoteFileAdd(getPath());
	}
	
	private void notifyRemoteFileAdd(final Path path){
		if (getMessageBus() != null) {
			getMessageBus().publish(new RemoteFileAddedMessage(path));
		}
	}
}

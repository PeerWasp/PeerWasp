package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.RemoteFileUpdatedMessage;
import org.peerbox.events.MessageBus;

public class RemoteFileUpdateListener extends FileOperationListener{

	public RemoteFileUpdateListener(Path path, MessageBus messageBus) {
		super(path, messageBus);
	}
	
	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyRemoteFileUpdate(getPath());
	}
	
	private void notifyRemoteFileUpdate(final Path path){
		if (getMessageBus() != null) {
			getMessageBus().publish(new RemoteFileUpdatedMessage(path));
		}
	}

}

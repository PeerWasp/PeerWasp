package org.peerbox.watchservice.states.listeners;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.messages.RemoteFileUpdatedMessage;
import org.peerbox.events.MessageBus;

public class RemoteFileUpdateListener extends FileOperationListener{

	public RemoteFileUpdateListener(FileInfo file, MessageBus messageBus) {
		super(file, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyRemoteFileUpdate(getFile());
	}

	private void notifyRemoteFileUpdate(final FileInfo file){
		if (getMessageBus() != null) {
			getMessageBus().publish(new RemoteFileUpdatedMessage(file));
		}
	}

}

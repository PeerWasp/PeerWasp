package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.RemoteFileUpdatedMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class RemoteFileUpdateListener extends FileOperationListener{

	public RemoteFileUpdateListener(FileHelper file, MessageBus messageBus) {
		super(file, messageBus);
	}
	
	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyRemoteFileUpdate(getFile());
	}
	
	private void notifyRemoteFileUpdate(final FileHelper file){
		if (getMessageBus() != null) {
			getMessageBus().publish(new RemoteFileUpdatedMessage(file));
		}
	}

}

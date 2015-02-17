package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.LocalFileDeletedMessage;
import org.peerbox.events.MessageBus;

public class LocalFileDeleteListener extends FileOperationListener{


	public LocalFileDeleteListener(final Path path, final MessageBus messageBus) {
		super(path, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileDelete(getPath());
	}
	
	private void notifyLocalFileDelete(final Path path){
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileDeletedMessage(path));
		}
	}


}

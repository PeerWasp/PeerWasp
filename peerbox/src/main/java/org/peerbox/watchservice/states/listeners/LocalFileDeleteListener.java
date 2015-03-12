package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.messages.LocalFileDeletedMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileDeleteListener extends FileOperationListener{


	public LocalFileDeleteListener(final FileHelper file, final MessageBus messageBus) {
		super(file, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileDelete(getFile());
	}
	
	private void notifyLocalFileDelete(final FileHelper file){
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileDeletedMessage(file));
		}
	}


}

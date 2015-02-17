package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import net.engio.mbassy.bus.common.IMessageBus;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.LocalFileUpdatedMessage;
import org.peerbox.events.MessageBus;

public class LocalFileUpdateListener extends FileOperationListener {

	public LocalFileUpdateListener(final Path path, MessageBus messageBus) {
		super(path, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileUpdate(getPath());
	}
	
	private void notifyLocalFileUpdate(Path path){
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileUpdatedMessage(path));
		}
	}

}
package org.peerbox.watchservice.states.listeners;

import net.engio.mbassy.bus.common.IMessageBus;

import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.LocalFileUpdatedMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileUpdateListener extends FileOperationListener {

	public LocalFileUpdateListener(final FileHelper file, MessageBus messageBus) {
		super(file, messageBus);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		super.onExecutionSucceeded(args);
		notifyLocalFileUpdate(getFile());
	}
	
	private void notifyLocalFileUpdate(FileHelper file){
		if (getMessageBus() != null) {
			getMessageBus().publish(new LocalFileUpdatedMessage(file));
		}
	}

}
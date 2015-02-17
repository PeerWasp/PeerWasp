package org.peerbox.presenter.settings.synchronization.eventbus;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.manager.file.LocalFileDesyncMessage;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.events.IMessageListener;
import org.peerbox.presenter.settings.synchronization.messages.FileExecutionStartedMessage;
import org.peerbox.presenter.settings.synchronization.messages.FileExecutionSucceededMessage;

public interface IExecutionMessageListener extends IMessageListener {

	@Handler
	void onExecutionStarts(FileExecutionStartedMessage message);
	
	@Handler
	void onExecutionSucceeds(FileExecutionSucceededMessage message);
	
	@Handler
	void onExecutionFails(FileExecutionFailedMessage message);
	
	@Handler
	void onFileSoftDeleted(LocalFileDesyncMessage message);
	
}

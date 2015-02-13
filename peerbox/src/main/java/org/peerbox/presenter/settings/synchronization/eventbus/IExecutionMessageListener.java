package org.peerbox.presenter.settings.synchronization.eventbus;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.manager.file.FileDesyncMessage;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.events.IMessageListener;
import org.peerbox.presenter.settings.synchronization.messages.ExecutionStartsMessage;
import org.peerbox.presenter.settings.synchronization.messages.ExecutionSuccessfulMessage;

public interface IExecutionMessageListener extends IMessageListener {

	@Handler
	void onExecutionStarts(ExecutionStartsMessage message);
	
	@Handler
	void onExecutionSucceeds(ExecutionSuccessfulMessage message);
	
	@Handler
	void onExecutionFails(FileExecutionFailedMessage message);
	
	@Handler
	void onFileSoftDeleted(FileDesyncMessage message);
	
}

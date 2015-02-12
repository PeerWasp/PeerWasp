//package org.peerbox.presenter.settings.synchronization.eventbus;
//
//import net.engio.mbassy.listener.Handler;
//
//import org.peerbox.app.manager.file.FileExecutionFailedMessage;
//import org.peerbox.presenter.settings.synchronization.messages.ExecutionStartsMessage;
//import org.peerbox.presenter.settings.synchronization.messages.ExecutionSuccessfulMessage;
//import org.peerbox.watchservice.Action;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class ExecutionMessageListener implements IExecutionMessageListener{
//
//	private final static Logger logger = LoggerFactory.getLogger(ExecutionMessageListener.class);
//	
//	@Override
//	@Handler
//	public void onExecutionStarts(ExecutionStartsMessage message) {
//		logger.trace("onExecutionStarts: {}", message.getPath());
//	}
//
//	@Override
//	@Handler
//	public void onExecutionSucceeds(ExecutionSuccessfulMessage message) {
//		logger.trace("onExecutionSucceeds: {}", message.getPath());
//	}
//
//	@Override
//	@Handler
//	public void onExecutionFails(FileExecutionFailedMessage message) {
//		logger.trace("onExecutionFails: {}", message.getPath());
//	}
//
//}

package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.events.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOperationListener implements IProcessComponentListener {

	private static final Logger logger = LoggerFactory.getLogger(FileOperationListener.class);
	private final Path path;
	private MessageBus messageBus;
	FileOperationListener(final Path path, MessageBus messageBus) {
		this.path = path;
		this.messageBus = messageBus;
	}

	public Path getPath() {
		return path;
	}
	
	public MessageBus getMessageBus(){
		return messageBus;
	}

	@Override
	public void onExecuting(IProcessEventArgs args) {
		logger.trace("onExecuting: {}", path);
	}

	@Override
	public void onRollbacking(IProcessEventArgs args) {
		logger.trace("onRollbacking: {}", path);
	}

	@Override
	public void onPaused(IProcessEventArgs args) {
		logger.trace("onPaused: {}", path);
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		logger.trace("onExecutionSucceeded: {}", path);
	}

	@Override
	public void onExecutionFailed(IProcessEventArgs args) {
		logger.trace("onExecutionFailed: {}", path);
	}

	@Override
	public void onRollbackSucceeded(IProcessEventArgs args) {
		logger.trace("onRollbackSucceeded: {}", path);
	}

	@Override
	public void onRollbackFailed(IProcessEventArgs args) {
		logger.trace("onRollbackFailed: {}", path);
	}
}

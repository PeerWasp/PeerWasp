package org.peerbox.watchservice.states.listeners;

import java.nio.file.Path;

import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOperationListener implements IProcessComponentListener {

	private static final Logger logger = LoggerFactory.getLogger(FileOperationListener.class);
	private final FileHelper file;
	private MessageBus messageBus;
	FileOperationListener(final FileHelper file, MessageBus messageBus) {
		this.file = file;
		this.messageBus = messageBus;
	}

	public FileHelper getFile() {
		return file;
	}
	
	public MessageBus getMessageBus(){
		return messageBus;
	}

	@Override
	public void onExecuting(IProcessEventArgs args) {
		logger.trace("onExecuting: {}", file.getPath());
	}

	@Override
	public void onRollbacking(IProcessEventArgs args) {
		logger.trace("onRollbacking: {}", file.getPath());
	}

	@Override
	public void onPaused(IProcessEventArgs args) {
		logger.trace("onPaused: {}", file.getPath());
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		logger.trace("onExecutionSucceeded: {}", file.getPath());
	}

	@Override
	public void onExecutionFailed(IProcessEventArgs args) {
		logger.trace("onExecutionFailed: {}", file.getPath());
	}

	@Override
	public void onRollbackSucceeded(IProcessEventArgs args) {
		logger.trace("onRollbackSucceeded: {}", file.getPath());
	}

	@Override
	public void onRollbackFailed(IProcessEventArgs args) {
		logger.trace("onRollbackFailed: {}", file.getPath());
	}
}

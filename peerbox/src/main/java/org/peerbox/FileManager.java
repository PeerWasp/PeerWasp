package org.peerbox;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.h2h.ProcessHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileManager implements IPeerboxFileManager{

	private static final Logger logger = LoggerFactory.getLogger(FileManager.class);
	private IFileManager h2hFileManager;

	public FileManager(final IFileManager h2hFileManager) {
		this.h2hFileManager = h2hFileManager;
	}

	public ProcessHandle<Void> add(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("ADD - {}", file);
		IProcessComponent<Void> component = h2hFileManager.createAddProcess(file);
		component.attachListener(new FileOperationListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	public ProcessHandle<Void> update(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("UPDATE - {}", file);
		IProcessComponent<Void> component = h2hFileManager.createUpdateProcess(file);
		component.attachListener(new FileOperationListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	public ProcessHandle<Void> delete(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("DELETE - {}", file);
		IProcessComponent<Void> component = h2hFileManager.createDeleteProcess(file);
		component.attachListener(new FileOperationListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	public ProcessHandle<Void> move(final File source, final File destination) throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("MOVE - from: {}, to: {}", source, destination);
		IProcessComponent<Void> component = h2hFileManager.createMoveProcess(source, destination);
		component.attachListener(new FileOperationListener(source));
		component.attachListener(new FileOperationListener(destination));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	public ProcessHandle<Void> download(final File file) throws NoSessionException, NoPeerConnectionException {
		IProcessComponent<Void> component = h2hFileManager.createDownloadProcess(file);
		component.attachListener(new FileOperationListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	// TODO(AA): return async handle
	public ProcessHandle<Void> recover(final File file, final IVersionSelector versionSelector) throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("RECOVER - {}", file);
		IProcessComponent<Void> component = h2hFileManager.createRecoverProcess(file,
				versionSelector);
		component.executeAsync();
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}
	
	// TODO(AA): return async handle
	public IProcessComponent<Void> share(final File folder, final String userId, final PermissionType permission) throws IllegalArgumentException, NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("SHARE - User: '{}', Permission: '{}', Folder: '{}'", userId,
				permission.name(), folder);

		IProcessComponent<Void> component = h2hFileManager.createShareProcess(folder, userId, permission);
		component.attachListener(new FileOperationListener(folder));
		component.executeAsync();
		return component;
	}
	
	public FileNode listFiles(IProcessComponentListener listener) throws IllegalArgumentException, NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		IProcessComponent<FileNode> component = h2hFileManager.createFileListProcess();
		component.attachListener(listener);
		return component.execute();
	}

	public FileNode listFiles() throws IllegalArgumentException, NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		IProcessComponent<FileNode> component = h2hFileManager.createFileListProcess();
		return component.execute();
	}


	private final class FileOperationListener implements IProcessComponentListener {
		private final File path;

		public FileOperationListener(final File path) {
			this.path = path;
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
}
package org.peerbox;

import java.io.File;

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
import org.peerbox.app.manager.AbstractManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.h2h.ProcessHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


public class FileManager extends AbstractManager implements IPeerboxFileManager {

	private static final Logger logger = LoggerFactory.getLogger(FileManager.class);

	@Inject
	public FileManager(final INodeManager h2hManager) {
		super(h2hManager, null); // TODO(AA): give message bus instance
	}
	
	@Override
	public ProcessHandle<Void> add(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("ADD - {}", file);
		IProcessComponent<Void> component = getFileManager().createAddProcess(file);
		component.attachListener(new FileOperationListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> update(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("UPDATE - {}", file);
		IProcessComponent<Void> component = getFileManager().createUpdateProcess(file);
		component.attachListener(new FileOperationListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> delete(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("DELETE - {}", file);
		IProcessComponent<Void> component = getFileManager().createDeleteProcess(file);
		component.attachListener(new FileOperationListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> move(final File source, final File destination) throws NoSessionException, NoPeerConnectionException {
		logger.debug("MOVE - from: {}, to: {}", source, destination);
		IProcessComponent<Void> component = getFileManager().createMoveProcess(source, destination);
		component.attachListener(new FileOperationListener(source));
		component.attachListener(new FileOperationListener(destination));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> download(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("DOWNLOAD - {}", file);
		IProcessComponent<Void> component = getFileManager().createDownloadProcess(file);
		component.attachListener(new FileOperationListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> recover(final File file, final IVersionSelector versionSelector) throws NoSessionException, NoPeerConnectionException, IllegalArgumentException {
		logger.debug("RECOVER - {}", file);
		IProcessComponent<Void> component = getFileManager().createRecoverProcess(file, versionSelector);
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}
	
	@Override
	public ProcessHandle<Void> share(final File folder, final String userId, final PermissionType permission) throws IllegalArgumentException, NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("SHARE - User: '{}', Permission: '{}', Folder: '{}'", userId, permission.name(), folder);
		IProcessComponent<Void> component = getFileManager().createShareProcess(folder, userId, permission);
		component.attachListener(new FileOperationListener(folder));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}
	
	@Override
	public FileNode listFiles(IProcessComponentListener listener) throws NoPeerConnectionException, NoSessionException, InvalidProcessStateException, ProcessExecutionException {
		IProcessComponent<FileNode> component = getFileManager().createFileListProcess();
		component.attachListener(listener);
		return component.execute();
	}

	@Override
	public FileNode listFiles() throws NoPeerConnectionException, NoSessionException, InvalidProcessStateException, ProcessExecutionException {
		IProcessComponent<FileNode> component = getFileManager().createFileListProcess();
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
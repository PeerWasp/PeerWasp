package org.peerbox;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

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
import org.peerbox.watchservice.FileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: do we need to buffer the operations somehow e.g. such that not unlimited add operations run in parallel?
 * (encryption, chunking, uploading etc. consumes resources and it may better to have a limit for that).
 * ActionExecutor may also schedule it accordingly.
 *
 */

public class FileManager {

	private static final Logger logger = LoggerFactory.getLogger(FileManager.class);
	private IFileManager h2hFileManager;
	private FileEventManager fileEventManager;

	public FileManager(IFileManager h2hFileManager) {
		this.h2hFileManager = h2hFileManager;
	}
	
	public FileManager(IFileManager h2hFileManager, FileEventManager fileEventManager){
		this.h2hFileManager = h2hFileManager;
		this.fileEventManager = fileEventManager;
	}

	public IProcessComponent<Void> add(File file) throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("ADD - {}", file);
		IProcessComponent<Void> component = h2hFileManager.createAddProcess(file);
		component.attachListener(new FileOperationListener(file));
		component.executeAsync();
		return component;
	}

	public IProcessComponent<Void> update(File file) throws NoSessionException, IllegalArgumentException,
			NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("UPDATE - {}", file);
		IProcessComponent<Void> component = h2hFileManager.createUpdateProcess(file);
		component.attachListener(new FileOperationListener(file));
		component.executeAsync();
		return component;
	}

	public IProcessComponent<Void> delete(File file) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("DELETE - {}", file);
		IProcessComponent<Void> component = h2hFileManager.createDeleteProcess(file);
		component.attachListener(new FileOperationListener(file));
		component.executeAsync();
		return component;
	}

	public IProcessComponent<Void> move(File source, File destination) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("MOVE - from: {}, to: {}", source, destination);
		// TODO: implement move.
		IProcessComponent<Void> component = h2hFileManager.createMoveProcess(source, destination);
		component.attachListener(new FileOperationListener(source));
		component.attachListener(new FileOperationListener(destination));
		component.executeAsync();
		return component;
	}

	public IProcessComponent<Void> recover(File file, IVersionSelector versionSelector) throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("RECOVER - {}", file);
		IProcessComponent<Void> component = h2hFileManager.createRecoverProcess(file, versionSelector);
		component.executeAsync();
		return component;
	}
	
	public IProcessComponent<Void> download(File file) throws NoSessionException, IllegalArgumentException,
	NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		IProcessComponent<Void> component = h2hFileManager.createDownloadProcess(file);
		component.attachListener(new FileOperationListener(file));
		component.executeAsync();
		return component;
	}

	public IProcessComponent<Void> share(File folder, String userId, PermissionType permission)
			throws IllegalArgumentException, NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("SHARE - User: '{}', Permission: '{}', Folder: '{}'", userId, permission.name(), folder);
		
		IProcessComponent<Void> component = h2hFileManager.createShareProcess(folder,  userId, permission);
		component.attachListener(new FileOperationListener(folder));
		component.executeAsync();
		return component;
	}

	public IProcessComponent<FileNode> getFileList() {
		// createFileListProcess
		throw new RuntimeException("Not implemented!");
	}
	
	
	private class FileOperationListener implements IProcessComponentListener {
		private File path;
		public FileOperationListener(File path) {
			this.path = path;
		}
		
		@Override
		public void onExecuting(IProcessEventArgs args) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRollbacking(IProcessEventArgs args) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPaused(IProcessEventArgs args) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onExecutionSucceeded(IProcessEventArgs args) {
			logger.debug("Operation succeeded: {}", path);
		}

		@Override
		public void onExecutionFailed(IProcessEventArgs args) {
			logger.debug("Operation failed: {}", path);
		}

		@Override
		public void onRollbackSucceeded(IProcessEventArgs args) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRollbackFailed(IProcessEventArgs args) {
			// TODO Auto-generated method stub
			
		}
	}

	public void setFileEventManager(FileEventManager fileEventManager) {
		this.fileEventManager = fileEventManager;
	}

	public IFileManager getH2HFileManager() {
		return h2hFileManager;
	}
}
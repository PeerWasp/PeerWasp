package org.peerbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileTaste;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IResultProcessComponent;
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

	public FileManager(IFileManager h2hFileManager) {
		this.h2hFileManager = h2hFileManager;
	}

	public IProcessComponent add(File file) throws NoSessionException, NoPeerConnectionException,
			IllegalFileLocation, InvalidProcessStateException {
		logger.debug("ADD - {}", file);
		IProcessComponent component = h2hFileManager.add(file);
		component.attachListener(new FileOperationListener(file));
		component.start();
		return component;
	}

	public IProcessComponent update(File file) throws NoSessionException, IllegalArgumentException,
			NoPeerConnectionException, InvalidProcessStateException {
		logger.debug("UPDATE - {}", file);
		IProcessComponent component = h2hFileManager.update(file);
		component.attachListener(new FileOperationListener(file));
		component.start();
		return component;
	}

	public IProcessComponent delete(File file) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException {
		logger.debug("DELETE - {}", file);
		IProcessComponent component = h2hFileManager.delete(file);
		component.attachListener(new FileOperationListener(file));
		component.start();
		return component;
	}

	public IProcessComponent move(File source, File destination) throws NoSessionException,
			NoPeerConnectionException {
		logger.debug("MOVE - from: {}, to: {}", source, destination);
		// TODO: implement move.
		return null;
	}

	public IProcessComponent recover(File file, IVersionSelector versionSelector)
			throws FileNotFoundException, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException {
		logger.debug("RECOVER - {}", file);
		// TODO: implement recover
		return null;
	}

	public IProcessComponent share(File folder, String userId, PermissionType permission)
			throws IllegalFileLocation, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException {
		logger.debug("SHARE");
		// TODO: implement sharing
		return null;
	}

	public IResultProcessComponent<List<FileTaste>> getFileList() throws NoSessionException {
		return null;
	}
	
	
	private class FileOperationListener implements IProcessComponentListener {
		private File path;
		public FileOperationListener(File path) {
			this.path = path;
		}
		
		@Override
		public void onSucceeded() {
			logger.debug("Operation succeeded: {}", path);
		}
		@Override
		public void onFailed(RollbackReason reason) {
			logger.debug("Operation failed: {} ({})", path, reason.getHint());
		}
	}
}

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
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.PathUtils;
import org.peerbox.watchservice.PeerboxVersionSelector;
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
			NoPeerConnectionException, InvalidProcessStateException {
		logger.debug("MOVE - from: {}, to: {}", source, destination);
		// TODO: implement move.
		IProcessComponent component = h2hFileManager.move(source, destination);
		component.attachListener(new FileOperationListener(source));
		component.attachListener(new FileOperationListener(destination));
		component.start();
		return component;
	}

	public IProcessComponent recover(File file, IVersionSelector versionSelector) throws FileNotFoundException, NoSessionException, NoPeerConnectionException, InvalidProcessStateException {
		logger.debug("RECOVER - {}", file);
		IProcessComponent component = h2hFileManager.recover(file, versionSelector);
		//component.attachListener(new FileRecoveryListener(file, versionSelector.getVersionToRecover()));
		//logger.trace("listener attached.");
		component.start();
		return component;
	}
	
	public IProcessComponent download(File file) throws NoSessionException, IllegalArgumentException,
	NoPeerConnectionException, InvalidProcessStateException {
		IProcessComponent component = h2hFileManager.download(file);
		component.attachListener(new FileOperationListener(file));
		component.start();
		return component;
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
	
	
	// TODO(CA): still needed?
//	private class FileRecoveryListener implements IProcessComponentListener {
//		private File file;
//		public FileRecoveryListener(File file, int version) {
//			this.file = PathUtils.getRecoveredFilePath(file.getPath(), version).toFile();
//		}
//
//		@Override
//		public void onSucceeded() {
//			logger.debug("Recovery Operation succeeded: {}", file);
//			if(!file.exists()){
//				logger.error("File {} does not exist after recovery.", file);
//			} else {
//
//				if(fileEventManager == null){
//					logger.error("fileEventManager is null! Recovered file {} is not added.", file);
//				} else {
//					logger.info("File {} has been added after successful recovery.", file);
//					fileEventManager.onLocalFileCreated(file.toPath(), false);
//					try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					fileEventManager.onLocalFileCreated(file.toPath(), false);
//				}
//				
//				IProcessComponent process;
//				try {
//					process = add(path);
//					if(process != null){
//						//process.attachListener(new FileManagerProcessListener());
//						//process.start();
//					} else {
//						System.err.println("process is null");
//					}
//				} catch (NoSessionException | NoPeerConnectionException | IllegalFileLocation
//						| InvalidProcessStateException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//			
//		}
//		@Override
//		public void onFailed(RollbackReason reason) {
//			logger.debug("Operation failed: {} ({})", file, reason.getHint());
//		}
//	}

	public void setFileEventManager(FileEventManager fileEventManager) {
		this.fileEventManager = fileEventManager;
	}

	public IFileManager getH2HFileManager() {
		return h2hFileManager;
	}
}
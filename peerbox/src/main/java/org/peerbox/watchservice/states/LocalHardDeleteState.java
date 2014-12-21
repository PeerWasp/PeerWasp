package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.FileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.h2h.ProcessHandle;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.FileComponent;
import org.peerbox.watchservice.IFileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalHardDeleteState extends AbstractActionState{
	
	private final static Logger logger = LoggerFactory.getLogger(LocalHardDeleteState.class);
	
	public LocalHardDeleteState(Action action) {
		super(action, StateType.LOCAL_HARD_DELETE);
	}

	public AbstractActionState getDefaultState() {
		logger.debug("Return to default state 'InitialState' as component was removed completely {}",
				action.getFilePath());
		return new InitialState(action);
	}
	
	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		return new LocalUpdateState(action); //e.g. hard delete -> Ctrl + Z;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		return new LocalUpdateState(action); //e.g. hard delete -> Ctrl + Z;
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		return new InitialState(action); //File has already been deleted, finish.
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		return new RemoteCreateState(action); // The network wins
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		return new RemoteUpdateState(action); // The network wins
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		return this; // TODO: remote create at target location!
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		throw new NotImplException("LocalHardDeleteState.handleLocalCreate()");
	}
	
	public AbstractActionState handleLocalDelete(){
		logger.trace("File {}: entered handleLocalDelete", action.getFilePath());
		IFileEventManager eventManager = action.getEventManager();
		eventManager.getFileComponentQueue().remove(action.getFile());
//		if(action.getFile().isFile()){
//			String oldHash = action.getFile().getContentHash();
//			action.getFile().updateContentHash();
//			logger.debug("File: {}Previous content hash: {} new content hash: ", action.getFilePath(), oldHash, action.getFile().getContentHash());
//			SetMultimap<String, FileComponent> deletedFiles = action.getEventManager().getDeletedFileComponents();
//			deletedFiles.put(action.getFile().getContentHash(), action.getFile());
//			logger.debug("Put deleted file {} with hash {} to SetMultimap<String, FileComponent>", action.getFilePath(), action.getFile().getContentHash());
//		} else {
//
//			Map<String, FolderComposite> deletedFolders = eventManager.getDeletedByContentNamesHash();
//			logger.debug("Added folder {} with structure hash {} to deleted folders.", action.getFilePath(), action.getFile().getStructureHash());
//			deletedFolders.put(action.getFile().getStructureHash(), (FolderComposite)action.getFile());
//		}
		FileComponent comp = eventManager.getFileTree().deleteComponent(action.getFile().getPath().toString());
//		logger.debug("After delete hash of {} is {}", comp.getPath(), comp.getStructureHash());
//		eventManager.getFileComponentQueue().add(action.getFile());
		updateTimeAndQueue();
		return changeStateOnLocalDelete();
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		throw new NotImplException("LocalHardDeleteState.handleLocalUpdate()");
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldFilePath) {
		throw new NotImplException("LocalHardDeleteState.handleLocalMove()");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		throw new NotImplException("LocalHardDeleteState.handleRemoteCreate()");
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		throw new NotImplException("LocalHardDeleteState.handleRemoteDelete()");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		throw new NotImplException("LocalHardDeleteState.handleRemoteUpdate()");
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		throw new NotImplException("LocalHardDeleteState.handleRemoteMove()");
	}

	@Override
	public ExecutionHandle execute(FileManager fileManager) throws InvalidProcessStateException, ProcessExecutionException, NoSessionException, NoPeerConnectionException {
		Path path = action.getFilePath();
		logger.debug("Execute LOCAL DELETE: {}", path);
		ProcessHandle<Void> handle = fileManager.delete(path.toFile());
		if (handle != null && handle.getProcess() != null) {
			handle.getProcess().attachListener(new FileManagerProcessListener());
			handle.executeAsync();
		} else {
			System.err.println("handle or process is null.");
		}
		
		return new ExecutionHandle(action, handle);
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleLocalRecover(int version) {
		// TODO Auto-generated method stub
		throw new NotImplException("LocalHardDeleteState.handleLocalRecover");
	}
}

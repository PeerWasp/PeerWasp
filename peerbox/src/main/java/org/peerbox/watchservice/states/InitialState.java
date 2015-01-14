package org.peerbox.watchservice.states;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the Initial state is given when a file is considered as new, synced or unknown.
 * The transition to another state is always valid and will be therefore accepted.
 *
 * @author winzenried
 *
 */
public class InitialState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(InitialState.class);

	public InitialState(Action action) {
		super(action, StateType.INITIAL);
	}


//	@Override
//	public AbstractActionState changeStateOnLocalDelete() {
//		logStateTransission(getStateType(), EventType.LOCAL_DELETE, StateType.LOCAL_DELETE);
////		return new LocalDeleteState(action);
//		return this;
//	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path source) {
		logStateTransission(getStateType(), EventType.LOCAL_MOVE, StateType.LOCAL_MOVE);
		return new LocalMoveState(action, source);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Initial -> Remote Move ({}) {}", action.getFile().getPath(), action.hashCode());

		Path path = action.getFile().getPath();
		logger.debug("Execute REMOTE MOVE: {}", path);
		throw new NotImplException("InitialState.onremoteMove");
	}

	@Override
	public AbstractActionState handleLocalCreate() {

		IFileEventManager eventManager = action.getEventManager();
		if(action.getFile().getPath().toFile().isDirectory()){
			//find deleted by structure hash
			Map<String, FolderComposite> deletedFolders = eventManager.getFileTree().getDeletedByContentNamesHash();
			String structureHash = action.getFile().getStructureHash();
			logger.trace("LocalCreate: structure hash of {} is {}", action.getFile().getPath(), structureHash);
			FolderComposite moveSource = deletedFolders.get(structureHash);
			if(moveSource != null){
				eventManager.getFileTree().deleteFile(moveSource.getPath());
				logger.trace("Folder move detected from {} to {}", moveSource.getPath(), action.getFile().getPath());
				moveSource.getAction().handleLocalMoveEvent(action.getFile().getPath());
				eventManager.getFileComponentQueue().remove(action.getFile());
				//TODO: cleanup filecomponentqueue: remove children of folder if in localcreate state!
				return changeStateOnLocalMove(action.getFile().getPath());
			}
		}
		logger.trace("Before: File {} content {}", action.getFile().getPath(), action.getFile().getContentHash());
		eventManager.getFileTree().putFile(action.getFile().getPath(), action.getFile());
		action.getFile().bubbleContentHashUpdate();//updateContentHash();
		logger.trace("After: File {} content {}", action.getFile().getPath(), action.getFile().getContentHash());

		FileComponent moveSource = eventManager.getFileTree().findDeletedByContent(action.getFile());
		logger.debug("File {} has hash {}", action.getFile().getPath(), action.getFile().getContentHash());
		if(moveSource == null){
//			eventManager.getFileTree().putComponent(action.getFilePath().toString(), action.getFile());
//			eventManager.getFileTree().putFile(action.getFilePath(), action.getFile());
			if(action.getFile().isUploaded()){
				logger.debug("This file is already uploaded, hence it is not uploaded again.");
				updateTimeAndQueue();
				return changeStateOnLocalUpdate();
			}
			logger.trace("Handle regular create of {}, as no possible move source has been found.", action.getFile().getPath());
			updateTimeAndQueue();
			return changeStateOnLocalCreate();
		} else {
			eventManager.getFileTree().deleteFile(moveSource.getPath());
			eventManager.getFileComponentQueue().remove(action.getFile());
			if(moveSource.isUploaded()){
				logger.trace("Handle move of {}, from {}.", action.getFile().getPath(), moveSource.getPath());
//				eventManager.getFileTree().deleteFile(action.getFile().getPath());
				moveSource.getAction().handleLocalMoveEvent(action.getFile().getPath());
				return changeStateOnLocalMove(action.getFile().getPath());
			} else {
				eventManager.getFileTree().putFile(action.getFile().getPath(), action.getFile());
				updateTimeAndQueue();
				return changeStateOnLocalCreate();
			}
		}

	}

	@Override
	public AbstractActionState handleLocalDelete() {
		//throw new NotImplException("InitialState.handleLocalDelete");
		logger.debug("Local Delete is ignored i InitialState for {}", action.getFile().getPath());
		return this;
	}

	@Override
	public AbstractActionState handleLocalMove(Path newPath) {
		System.out.println("newPath: " + newPath);
		Path oldPath = action.getFile().getPath();
		action.getFile().setPath(newPath);
		action.getEventManager().getFileTree().putFile(newPath, action.getFile());
		updateTimeAndQueue();
		return changeStateOnLocalMove(oldPath);
//		throw new NotImplException("InitialState.handleLocalMove");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		logger.trace("{}", action.getEventManager().getFileTree().getClass().toString());
//		action.getEventManager().getFileTree().putComponent(action.getFilePath().toString(), action.getFile());
		action.getEventManager().getFileTree().putFile(action.getFile().getPath(), action.getFile());
		updateTimeAndQueue();
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		// TODO Auto-generated method stub
		throw new NotImplException("InitialState.handleRemoteDelete");
	}

	@Override
	public ExecutionHandle execute(IFileManager fileManager) throws NoSessionException,
			NoPeerConnectionException {
		logger.warn("Execute is not defined in the initial state  ({})", action.getFile().getPath());
		notifyActionExecuteSucceeded();
		return null;
	}

	@Override
	public AbstractActionState handleLocalRecover(File currentFile, int version) {
		// TODO Auto-generated method stub
		updateTimeAndQueue();
		return new RecoverState(action, currentFile, version);
	}

	public AbstractActionState getDefaultState(){
		return this;
	}
}

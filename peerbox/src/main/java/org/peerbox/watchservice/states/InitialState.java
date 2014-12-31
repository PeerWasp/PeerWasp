package org.peerbox.watchservice.states;

import java.nio.file.Path;
import java.util.Map;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.FileManager;
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

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logStateTransission(getStateType(), EventType.LOCAL_CREATE, StateType.LOCAL_CREATE);
		return new LocalCreateState(action);
	}

//	@Override
//	public AbstractActionState changeStateOnLocalDelete() {
//		logStateTransission(getStateType(), EventType.LOCAL_DELETE, StateType.LOCAL_DELETE);
////		return new LocalDeleteState(action);
//		return this;
//	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldPath) {
		logStateTransission(getStateType(), EventType.LOCAL_MOVE, StateType.INITIAL);
		return this;//new LocalMoveState(action, oldPath);
	}
	
	@Override
	public AbstractActionState changeStateOnLocalRecover(int versionToRecover) {
		//logStateTransission(getStateType(), EventType.LOCAL_RECOVER, StateType.LOCAL_RECOVER);
//		throw new NotImplException("InitialState.localRecover");
		return new RecoverState(action, versionToRecover);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logStateTransission(getStateType(), EventType.REMOTE_UPDATE, StateType.REMOTE_UPDATE);
		return new RemoteUpdateState(action);
	}
	
	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		logStateTransission(getStateType(), EventType.REMOTE_CREATE, StateType.REMOTE_CREATE);
		return new RemoteCreateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logStateTransission(getStateType(), EventType.REMOTE_DELETE, StateType.INITIAL);
		return this; //new RemoteDeleteState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Initial -> Remote Move ({}) {}", action.getFilePath(), action.hashCode());
		
		Path path = action.getFilePath();
		logger.debug("Execute REMOTE MOVE: {}", path);
		throw new NotImplException("InitialState.onremoteMove");
	}

	@Override
	public AbstractActionState handleLocalCreate() {

		IFileEventManager eventManager = action.getEventManager();
		if(action.getFilePath().toFile().isDirectory()){
			//find deleted by structure hash
			Map<String, FolderComposite> deletedFolders = eventManager.getFileTree().getDeletedByContentNamesHash();
			String structureHash = action.getFile().getStructureHash();
			logger.trace("LocalCreate: structure hash of {} is {}", action.getFilePath(), structureHash);
			FolderComposite moveSource = deletedFolders.get(structureHash);
			if(moveSource != null){
				logger.trace("Folder move detected from {} to {}", moveSource.getPath(), action.getFilePath());
				moveSource.getAction().handleLocalMoveEvent(action.getFilePath());
				eventManager.getFileComponentQueue().remove(action.getFile());
				//TODO: cleanup filecomponentqueue: remove children of folder if in localcreate state!
				return changeStateOnLocalMove(action.getFilePath());
			}
		}
		logger.trace("Before: File {} content {}", action.getFilePath(), action.getFile().getContentHash());
		eventManager.getFileTree().putFile(action.getFile());
		action.getFile().bubbleContentHashUpdate();//updateContentHash();
		logger.trace("After: File {} content {}", action.getFilePath(), action.getFile().getContentHash());

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileComponent moveSource = eventManager.getFileTree().findDeletedByContent(action.getFile());
		logger.debug("File {} has hash {}", action.getFilePath(), action.getFile().getContentHash());
		if(moveSource == null){
//			eventManager.getFileTree().putComponent(action.getFilePath().toString(), action.getFile());
//			eventManager.getFileTree().putFile(action.getFile());
			logger.trace("Handle regular create of {}, as no possible move source has been found.", action.getFilePath());
			updateTimeAndQueue();
			return changeStateOnLocalCreate();
		} else {
			logger.trace("Handle move of {}, from {}.", action.getFilePath(), moveSource.getPath());
			eventManager.getFileTree().deleteFile(action.getFilePath());
			moveSource.getAction().handleLocalMoveEvent(action.getFilePath());
			eventManager.getFileComponentQueue().remove(action.getFile());
			return changeStateOnLocalMove(action.getFilePath());
		}
		
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		//throw new NotImplException("InitialState.handleLocalDelete");
		logger.debug("Local Delete is ignored i InitialState for {}", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		throw new NotImplException("InitialState.handleLocalMove");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		logger.trace("{}", action.getEventManager().getFileTree().getClass().toString());
//		action.getEventManager().getFileTree().putComponent(action.getFilePath().toString(), action.getFile());
		action.getEventManager().getFileTree().putFile(action.getFile());
		updateTimeAndQueue();
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		// TODO Auto-generated method stub
		throw new NotImplException("InitialState.handleRemoteDelete");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		// TODO Auto-generated method stub
		throw new NotImplException("InitialState.handleRemoteUpdate");
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		// TODO Auto-generated method stub
		throw new NotImplException("InitialState.handleRemoteMove");
	}
	

	@Override
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException {
		logger.warn("Execute is not defined in the initial state  ({})", action.getFilePath());
		notifyActionExecuteSucceeded();
		return null;
	}

	@Override
	public AbstractActionState handleLocalRecover(int version) {
		// TODO Auto-generated method stub
		throw new NotImplException("InitialState.handleLocalRecover");
	}
	
	public AbstractActionState getDefaultState(){
		return this;
	}
}

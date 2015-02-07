package org.peerbox.watchservice.states;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;

/**
 * the Initial state is given when a file is considered as new, synced or unknown.
 * The transition to another state is always valid and will be therefore accepted.
 *
 * @author winzenried
 *
 */
public class InitialState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(InitialState.class);

	public InitialState(IAction action) {
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
		logStateTransition(getStateType(), EventType.LOCAL_MOVE, StateType.LOCAL_MOVE);
		return new LocalMoveState(action, source);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Initial -> Remote Move ({}) {}",
				action.getFile().getPath(), action.hashCode());

		Path path = action.getFile().getPath();
		logger.debug("Execute REMOTE MOVE: {}", path);
		throw new NotImplException("InitialState.onremoteMove");
	}

	@Override
	public AbstractActionState handleLocalCreate() {

		
		final IFileEventManager eventManager = action.getFileEventManager();
		final IFileTree fileTree = eventManager.getFileTree();
		final FileComponent file = action.getFile();
		final Path filePath = file.getPath();

		String oldContentHash = file.getContentHash();
		fileTree.putFile(filePath, file);
		file.bubbleContentHashUpdate();
		logger.debug("File {}: content hash update: '{}' -> '{}'", filePath, oldContentHash, file.getContentHash());

		if(action.getFile().isFile()){
			SetMultimap<String, FileComponent> createdByContentHash = action.getFileEventManager().getFileTree().getCreatedByContentHash();
			logger.trace("Put file {} with content hash {}", action.getFile().getPath(), action.getFile().getContentHash());
			createdByContentHash.put(action.getFile().getContentHash(), action.getFile());
		} else {
			SetMultimap<String, FolderComposite> createdByStructureHash = action.getFileEventManager().getFileTree().getCreatedByStructureHash();
			createdByStructureHash.put(action.getFile().getStructureHash(), (FolderComposite)action.getFile());
		}
		
		if (file.isFolder()) {
			// find deleted by structure hash
			String structureHash = file.getStructureHash();
			logger.trace("LocalCreate: structure hash of {} is {}", filePath, structureHash);
			FolderComposite moveSource = fileTree.findDeletedByStructure((FolderComposite)file);
			if (moveSource != null) {
				fileTree.deleteFile(moveSource.getPath());
				logger.trace("Folder move detected from {} to {}", moveSource.getPath(), filePath);
				moveSource.getAction().handleLocalMoveEvent(filePath);
				eventManager.getFileComponentQueue().remove(file);
				
				SetMultimap<String, FolderComposite> createdByStructureHash = action.getFileEventManager().getFileTree().getCreatedByStructureHash();
				boolean wasRemoved = createdByStructureHash.get(action.getFile().getStructureHash()).remove((FolderComposite)action.getFile());
				// TODO: cleanup filecomponentqueue: remove children of folder if in localcreate state!
				return changeStateOnLocalMove(filePath);
			}
		} else {
			FileComponent moveSource = fileTree.findDeletedByContent(file);
			if(moveSource != null){
//				if(moveSource.isSynchronized() == false){
//				updateTimeAndQueue();
//				return changeStateOnLocalCreate();
//			}
				fileTree.deleteFile(moveSource.getPath());
				eventManager.getFileComponentQueue().remove(file);
				
				String contentHash = action.getFile().getContentHash();
				boolean isRemoved = fileTree.getCreatedByContentHash().get(contentHash).remove(action.getFile());
				logger.trace("InitialState.handleLocalDelete: IsRemoved for file {} with hash {}: {}", action.getFile().getPath(), contentHash, isRemoved);
				
				if (moveSource.isUploaded()) {
					logger.trace("Handle move of {}, from {}.", filePath, moveSource.getPath());
					// eventManager.getFileTree().deleteFile(action.getFile().getPath());
					moveSource.getAction().handleLocalMoveEvent(filePath);
					return changeStateOnLocalMove(filePath);
				} else {
					logger.trace("No move of {}, as it was not uploaded.", moveSource.getPath());
					fileTree.putFile(filePath, file);
					updateTimeAndQueue();
					return changeStateOnLocalCreate();
				}
			}
		}

		if (file.isUploaded() && file.isSynchronized()) {
			logger.debug("File {} has been soft-deleted and recreated. This is regarded as a file update.", file.getPath());
//			ConflictHandler.resolveConflict(file.getPath(), true);
			updateTimeAndQueue();
			return changeStateOnLocalUpdate();
		}
		logger.trace("Handle regular create of {}, no move source has been found.", filePath);
		updateTimeAndQueue();
		return changeStateOnLocalCreate();
		
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		logger.debug("Local Delete is ignored in InitialState for {}", action.getFile().getPath());
		return this;
	}

	@Override
	public AbstractActionState handleLocalMove(Path newPath) {
		Path oldPath = action.getFile().getPath();
		action.getFile().setPath(newPath);
		action.getFileEventManager().getFileTree().putFile(newPath, action.getFile());
		updateTimeAndQueue();
		return changeStateOnLocalMove(oldPath);
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
//		logger.trace("{}", action.getFileEventManager().getFileTree().getClass().getSimpleName());
//		action.getEventManager().getFileTree().putComponent(action.getFilePath().toString(), action.getFile());
		action.getFileEventManager().getFileTree().putFile(action.getFile().getPath(), action.getFile());
		updateTimeAndQueue();
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		throw new NotImplException("InitialState.handleRemoteDelete");
	}

	@Override
	public ExecutionHandle execute(IFileManager fileManager) throws NoSessionException,
			NoPeerConnectionException {
		logger.warn("Execute is not defined in the initial state  ({})", action.getFile().getPath());
		return null;
	}

	public AbstractActionState getDefaultState() {
		return this;
	}
}

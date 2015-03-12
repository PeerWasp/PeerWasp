package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.file.messages.FileExecutionSucceededMessage;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FileLeaf;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;

/**
 * The Initial state is used when a file is considered as new, or known but
 * soft-deleted / de-synchronized.
 * @author winzenried
 */
public class InitialState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(InitialState.class);

	public InitialState(IAction action) {
		super(action, StateType.INITIAL);
	}

	public AbstractActionState getDefaultState() {
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path source) {
		logStateTransition(getStateType(), EventType.LOCAL_MOVE, StateType.LOCAL_MOVE);
		return new LocalMoveState(action, source);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate(){
		if(action.getFile().isUploaded()){
			logStateTransition(getStateType(), EventType.LOCAL_CREATE, StateType.ESTABLISHED);
			logger.trace("Re-creation of a softdeleted file. Ignore LocalCreate for  {}", action.getFile().getPath());
			return new EstablishedState(action);
		} else {
			logStateTransition(getStateType(), EventType.LOCAL_CREATE, StateType.LOCAL_CREATE);
			return new LocalCreateState(action);
		}
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logStateTransition(getStateType(), EventType.REMOTE_MOVE, StateType.ESTABLISHED);
		return new EstablishedState(action);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		final IFileTree fileTree = action.getFileEventManager().getFileTree();
		final FileComponent file = action.getFile();

		fileTree.putFile(file.getPath(), file);
		file.updateContentHash();

		addComponentToMoveTargetCandidates();

		if (file.isFolder()) {
			FolderComposite moveSource = fileTree.findDeletedByStructure((FolderComposite)file);
			if (moveSource != null) {
				return moveFolder(moveSource);
			}
		} else {
			FileLeaf moveSource = fileTree.findDeletedByContent((FileLeaf)file);
			if(moveSource != null){
				return moveFile(moveSource);
			}
		}
		if (file.isUploaded() && file.isSynchronized()) {
			return updateSoftDeletedFileComponent();
		} else {
			return localCreateDefaultHandling();
		}
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		logger.debug("Local Delete is ignored in InitialState for {}", action.getFile().getPath());
		return this;
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		FileComponent file = action.getFile();
		action.getFileEventManager().getFileTree().putFile(file.getPath(), file);
		logger.trace("put file {} to tree", file.getPath());
		updateTimeAndQueue();
		return changeStateOnRemoteCreate();
	}

	@Override
	public ExecutionHandle execute(IFileManager fileManager) throws NoSessionException,
			NoPeerConnectionException {
		logger.warn("Execute is not defined in the initial state  ({})", action.getFile().getPath());
		return null;
	}

	private void addComponentToMoveTargetCandidates() {
		if(action.getFile().isFile()){
			SetMultimap<String, FileComponent> createdByContentHash = action.getFileEventManager().getFileTree().getCreatedByContentHash();
			logger.trace("Put file {} with content hash {}", action.getFile().getPath(), action.getFile().getContentHash());
			createdByContentHash.put(action.getFile().getContentHash(), action.getFile());
		} else {
			SetMultimap<String, FolderComposite> createdByStructureHash = action.getFileEventManager().getFileTree().getCreatedByStructureHash();
			createdByStructureHash.put(action.getFile().getStructureHash(), (FolderComposite)action.getFile());
		}
	}

	private AbstractActionState localCreateDefaultHandling() {
		logger.trace("Handle regular create of {}, no move source has been found.", action.getFile().getPath());
		updateTimeAndQueue();
		return changeStateOnLocalCreate();
	}

	private AbstractActionState updateSoftDeletedFileComponent() {
		final FileComponent file = action.getFile();
		logger.debug("File {} has been soft-deleted and recreated. This is regarded as a file update.", file.getPath());
		if(file.isFolder()){
			logger.debug("Soft-deleted file {} is a folder. Ignore recreation", file.getPath());
			FileInfo fileHelper = new FileInfo(file);
			action.getFileEventManager().getMessageBus().publish(new FileExecutionSucceededMessage(fileHelper, file.getAction().getCurrentState().getStateType()));
			action.getFileEventManager().getFileComponentQueue().remove(action.getFile());
			return this;
		} else {
			updateTimeAndQueue();
			return changeStateOnLocalUpdate();
		}
	}

	private AbstractActionState moveFile(FileLeaf source) {
		final IFileTree fileTree = action.getFileEventManager().getFileTree();
		final FileComponent file = action.getFile();
		final Path filePath = file.getPath();

		fileTree.deleteFile(source.getPath());
		action.getFileEventManager().getFileComponentQueue().remove(file);

		String contentHash = action.getFile().getContentHash();
		boolean isRemoved = fileTree.getCreatedByContentHash().get(contentHash).remove(action.getFile());
		logger.trace("InitialState.handleLocalDelete: IsRemoved for file {} with hash {}: {}", action.getFile().getPath(), contentHash, isRemoved);

		if (source.isUploaded()) {
			logger.trace("Handle move of {}, from {}.", filePath, source.getPath());
			// eventManager.getFileTree().deleteFile(action.getFile().getPath());
			source.getAction().handleLocalMoveEvent(filePath);
			return this; //changeStateOnLocalMove(filePath);
		} else {
			logger.trace("No move of {}, as it was not uploaded.", source.getPath());
			fileTree.putFile(filePath, file);
			updateTimeAndQueue();
			return changeStateOnLocalCreate();
		}
	}


	private AbstractActionState moveFolder(FolderComposite source) {
		final IFileTree fileTree = action.getFileEventManager().getFileTree();
		final FileComponent file = action.getFile();
		final Path filePath = file.getPath();

		fileTree.deleteFile(source.getPath());
		logger.trace("Folder move detected from {} to {}", source.getPath(), filePath);
		source.getAction().handleLocalMoveEvent(filePath);
		action.getFileEventManager().getFileComponentQueue().remove(file);

		SetMultimap<String, FolderComposite> createdByStructureHash = action.getFileEventManager().getFileTree().getCreatedByStructureHash();
		boolean wasRemoved = createdByStructureHash.get(action.getFile().getStructureHash()).remove((FolderComposite)action.getFile());
		// TODO: cleanup filecomponentqueue: remove children of folder if in localcreate state!

		if (source.isUploaded()) {
			logger.trace("Handle move of {}, from {}.", filePath, source.getPath());
			// eventManager.getFileTree().deleteFile(action.getFile().getPath());
			source.getAction().handleLocalMoveEvent(filePath);
			return this; //changeStateOnLocalMove(filePath);
		} else {
			logger.trace("No move of {}, as it was not uploaded.", source.getPath());
			fileTree.putFile(filePath, file);
			updateTimeAndQueue();
			return changeStateOnLocalCreate();
		}
	}
}

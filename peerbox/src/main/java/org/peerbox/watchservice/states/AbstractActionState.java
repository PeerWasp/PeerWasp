package org.peerbox.watchservice.states;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.ProcessHandle;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.utils.NotImplementedException;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FileLeaf;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;

/**
 * Interface for different states of implemented state pattern
 *
 * @author winzenried
 *
 */
public abstract class AbstractActionState {
	private final static Logger logger = LoggerFactory.getLogger(AbstractActionState.class);
	protected IAction action;
	protected StateType type = StateType.ABSTRACT;
	protected ProcessHandle<Void> handle;

	/*
	 * Execution and notification related functions
	 */
	public abstract ExecutionHandle execute(IFileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException;
	
	public AbstractActionState(IAction action, StateType type) {
		this.action = action;
		this.type = type;
	}

	public StateType getStateType(){
		return type;
	}

	public AbstractActionState getDefaultState(){
		return new EstablishedState(action);
	}

	/*
	 * LOCAL state changers
	 */
	public AbstractActionState changeStateOnLocalCreate(){
		logStateTransition(getStateType(), EventType.LOCAL_CREATE, StateType.LOCAL_CREATE);
		return new LocalCreateState(action);
	}

	public AbstractActionState changeStateOnLocalDelete(){
		logStateTransition(getStateType(), EventType.LOCAL_DELETE, StateType.INITIAL);
		return new InitialState(action);
	}

	public AbstractActionState changeStateOnLocalUpdate(){
		logStateTransition(getStateType(), EventType.LOCAL_UPDATE, StateType.LOCAL_UPDATE);
		return new LocalUpdateState(action);
	}

	public AbstractActionState changeStateOnLocalMove(Path oldPath){
		logStateTransition(getStateType(), EventType.LOCAL_MOVE, StateType.LOCAL_MOVE);
		return new LocalMoveState(action, oldPath);
	}

	public AbstractActionState changeStateOnLocalHardDelete(){
		logStateTransition(getStateType(), EventType.LOCAL_HARD_DELETE, StateType.LOCAL_HARD_DELETE);
		return new LocalHardDeleteState(action);
	}

	/*
	 * REMOTE state changers
	 */
	public AbstractActionState changeStateOnRemoteDelete(){
		logStateTransition(getStateType(), EventType.REMOTE_DELETE, StateType.INITIAL);
		return new InitialState(action);
	}

	public AbstractActionState changeStateOnRemoteCreate(){
		logStateTransition(getStateType(), EventType.REMOTE_CREATE, StateType.REMOTE_CREATE);
		return new RemoteCreateState(action);
	}

	public AbstractActionState changeStateOnRemoteUpdate(){
		logStateTransition(getStateType(), EventType.REMOTE_UPDATE, StateType.REMOTE_UPDATE);
		return new RemoteUpdateState(action);
	}

	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logStateTransition(getStateType(), EventType.REMOTE_MOVE, StateType.ESTABLISHED);
		return new EstablishedState(action);
	}


	public AbstractActionState handleLocalCreate() {
		action.updateTimeAndQueue();
		return changeStateOnLocalCreate();
	}

	public AbstractActionState handleLocalHardDelete(){
		action.updateTimeAndQueue();
		return changeStateOnLocalHardDelete();
	}
	
	public AbstractActionState handleLocalDelete(){
		IFileTree fileTree = action.getFileEventManager().getFileTree();
		FileComponent file = action.getFile();
		
		action.updateTimeAndQueue();
		file.setIsSynchronized(false);
		
		if(file.isFile()){
			FileLeaf moveTarget = fileTree.findCreatedByContent((FileLeaf)file);
			if(moveTargetIsValid(moveTarget)){
				return performSwappedMove(moveTarget);
			} else {
				putToFileMoveSources((FileLeaf)file);
			}
		} else {
			FileComponent moveTarget = fileTree.findCreatedByStructure((FolderComposite)file);
			if(moveTargetIsValid(moveTarget)){
				return performSwappedMove(moveTarget);
			} else {
				putToFolderMoveSources((FolderComposite)file);
			}
		}
		
		file.getParent().updateContentHash();
		file.getParent().updateStructureHash();
		file.updateStateOnLocalDelete();
		return this.changeStateOnLocalDelete();
	}

	public AbstractActionState handleLocalUpdate() {
		action.updateTimeAndQueue();
		return changeStateOnLocalUpdate();
	}
	
	public AbstractActionState handleLocalMove(Path newPath) {
		Path oldPath = Paths.get(action.getFile().getPath().toString());
		action.getFile().setIsSynchronizedRecursively(true);
		action.getFileEventManager().getFileTree().putFile(newPath, action.getFile());
		action.updateTimeAndQueue();
		return changeStateOnLocalMove(oldPath);
	}

	/*
	 * REMOTE event handler
	 */

	public AbstractActionState handleRemoteCreate(){
		action.updateTimeAndQueue();
		return changeStateOnRemoteCreate();
	}

	public AbstractActionState handleRemoteDelete() {
		IFileEventManager eventManager = action.getFileEventManager();
		eventManager.getFileTree().deleteFile(action.getFile().getPath());
		eventManager.getFileComponentQueue().remove(action.getFile());

		try {
			java.nio.file.Files.delete(action.getFile().getPath());
		} catch (IOException e) {
			logger.warn("Could not delete file {} ({}).",
					action.getFile().getPath(), e.getMessage(), e);
		}
		return changeStateOnRemoteDelete();
	}

	public AbstractActionState handleRemoteUpdate() {
		action.updateTimeAndQueue();
		return changeStateOnRemoteUpdate();
	}

	public AbstractActionState handleRemoteMove(Path destPath) {
		final IFileEventManager eventManager = action.getFileEventManager();
		final IFileTree fileTree = eventManager.getFileTree();
		final FileComponent file = action.getFile();
		
		eventManager.getFileComponentQueue().remove(file);
		Path sourcePath = file.getPath();

		fileTree.deleteFile(file.getPath());
		fileTree.putFile(destPath, file);

		return changeStateOnRemoteMove(sourcePath);
	}

	private boolean moveTargetIsValid(FileComponent moveTarget){
		return moveTarget != null && moveTarget.getPath().toFile().exists();
	}

	private void putToFolderMoveSources(FolderComposite file) {
		final IFileTree fileTree = action.getFileEventManager().getFileTree();
		SetMultimap<String, FolderComposite> deletedFolders = fileTree.getDeletedByStructureHash();
		logger.trace("Delete folder: put folder {} with structure hash {} to deleted folders.", file.getPath(), file.getStructureHash());
		deletedFolders.put(file.getStructureHash(), (FolderComposite)file);
	}

	private void putToFileMoveSources(FileLeaf file) {
		final IFileTree fileTree = action.getFileEventManager().getFileTree();
		SetMultimap<String, FileComponent> deletedFiles = fileTree.getDeletedByContentHash();
		deletedFiles.put(file.getContentHash(), file);
		logger.debug("Put deleted file {} with hash {} to SetMultimap<String, FileComponent>", file.getPath(), file.getContentHash());
	}

	
	private AbstractActionState performSwappedMove(
			FileComponent moveTarget) {
		final IFileEventManager eventManager = action.getFileEventManager();
		logger.trace("We observed a swapped folder move (deletion of source file "
				+ "was reported after creation of target file: {} -> {}", action.getFile().getPath(), moveTarget.getPath());
		eventManager.getFileTree().deleteFile(action.getFile().getPath());
		eventManager.getFileComponentQueue().remove(moveTarget);
		return handleLocalMove(moveTarget.getPath());
	}

	protected void logStateTransition(StateType stateBefore, EventType event, StateType stateAfter){
		logger.debug("STATE_TRANSITION for file {}: {} + {} --> {}", action.getFile().getPath(),
				stateBefore.getName(), event.getString(), stateAfter.getName());
	}
}
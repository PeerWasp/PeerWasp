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
import org.peerbox.exceptions.NotImplException;
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

	public void updateTimeAndQueue(){
		action.getFileEventManager().getFileComponentQueue().remove(action.getFile());
		action.updateTimestamp();
		action.getFileEventManager().getFileComponentQueue().add(action.getFile());
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

	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath){
		throw new NotImplException(action.getCurrentState().getStateType().getName() + ".changeStateOnRemoteMove");
	}


	/**
	 * This handler is abstract because local create events are
	 * very state-sensitive and a default handling is not useful.
	 * @return
	 */
	public abstract AbstractActionState handleLocalCreate();

	public AbstractActionState handleLocalHardDelete(){
		updateTimeAndQueue();
		return changeStateOnLocalHardDelete();
	}
	
	public AbstractActionState handleLocalDelete(){
		IFileTree fileTree = action.getFileEventManager().getFileTree();
		FileComponent file = action.getFile();
		
		updateTimeAndQueue();
		file.setIsSynchronized(false);
		
		if(file.isFile()){
			FileLeaf moveTarget = fileTree.findCreatedByContent((FileLeaf)file);
			if(moveTargetIsValid(moveTarget)){
				return performSwappedFolderMove(moveTarget);
			} else {
				putToFileMoveSources((FileLeaf)file);
			}
		} else {
			FileComponent moveTarget = fileTree.findCreatedByStructure((FolderComposite)file);
			if(moveTargetIsValid(moveTarget)){
				return performSwappedFolderMove(moveTarget);
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
		updateTimeAndQueue();
		return changeStateOnLocalUpdate();
	}
	
	public AbstractActionState handleLocalMove(Path newPath) {
		Path oldPath = Paths.get(action.getFile().getPath().toString());
		action.getFileEventManager().getFileTree().putFile(newPath, action.getFile());
		updateTimeAndQueue();
		return changeStateOnLocalMove(oldPath);
	}

	/*
	 * REMOTE event handler
	 */

	public AbstractActionState handleRemoteCreate(){
		updateTimeAndQueue();
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
		updateTimeAndQueue();
		return changeStateOnRemoteUpdate();
	}

	public AbstractActionState handleRemoteMove(Path path){
		return changeStateOnRemoteMove(path);
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

	private AbstractActionState performSwappedFolderMove(
			FileComponent moveTarget) {
		final IFileEventManager eventManager = action.getFileEventManager();
		logger.trace("We observed a swapped folder move (deletion of source file "
				+ "was reported after creation of target file: {} -> {}", action.getFile().getPath(), moveTarget.getPath());
		eventManager.getFileTree().deleteFile(action.getFile().getPath());
		eventManager.getFileComponentQueue().remove(moveTarget);
		return handleLocalMove(moveTarget.getPath());
	}

	protected void logStateTransition(StateType stateBefore, EventType event, StateType stateAfter){
		logger.debug("File {}: {} + {}  --> {}", action.getFile().getPath(),
				stateBefore.getName(), event.getString(), stateAfter.getName());
	}
}
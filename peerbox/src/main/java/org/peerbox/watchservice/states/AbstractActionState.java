package org.peerbox.watchservice.states;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.FileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.h2h.ProcessHandle;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.IActionEventListener;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.composite.FileComponent;
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
	protected Action action;
	protected StateType type = StateType.ABSTRACT;
	protected ProcessHandle<Void> handle;

	public AbstractActionState(Action action, StateType type) {
		this.action = action;
		this.type = type;
	}
	
	public StateType getStateType(){
		return type;
	}
	
	public AbstractActionState getDefaultState(){
//		logger.debug("Return to default state 'EstablishedState': {}", action.getFile().getPath());
		return new EstablishedState(action);
	}
	
	public void updateTimeAndQueue(){
		action.getEventManager().getFileComponentQueue().remove(action.getFile());
		action.updateTimestamp();
		action.getEventManager().getFileComponentQueue().add(action.getFile());
	}
	
	protected void logStateTransission(StateType stateBefore, EventType event, StateType stateAfter){
		logger.debug("File {}: {} + {}  --> {}", action.getFilePath(), 
				stateBefore.getString(), event.getString(), stateAfter.getString());
	}
	
	public void performCleanup(){
		//nothing to do by default!
	}

	/*
	 * LOCAL state changers
	 */
	public AbstractActionState changeStateOnLocalCreate(){
		throw new NotImplException(action.getCurrentState().getStateType().getString() + ".changeStateOnLocalCreate");
	}

	public AbstractActionState changeStateOnLocalDelete(){
		throw new NotImplException(action.getCurrentState().getStateType().getString() + ".changeStateOnLocalDelete");
	}

	public AbstractActionState changeStateOnLocalUpdate(){
		throw new NotImplException(action.getCurrentState().getStateType().getString() + ".changeStateOnLocalUpdate");
	}

	public AbstractActionState changeStateOnLocalMove(Path oldPath){
		throw new NotImplException(action.getCurrentState().getStateType().getString() + ".changeStateOnLocalMove");
	}
	
	public AbstractActionState changeStateOnLocalRecover(int version){
		throw new NotImplException(action.getCurrentState().getStateType().getString() + ".changeStateOnLocalRecover");
	}
	
	public AbstractActionState changeStateOnLocalHardDelete(){
		return new LocalHardDeleteState(action);
	}

	/*
	 * REMOTE state changers
	 */
	public AbstractActionState changeStateOnRemoteDelete(){
		throw new NotImplException(action.getCurrentState().getStateType().getString() + ".changeStateOnRemoteDelete");
	}
	
	public AbstractActionState changeStateOnRemoteCreate(){
		throw new NotImplException(action.getCurrentState().getStateType().getString() + ".changeStateOnRemoteCreate");
	}

	public AbstractActionState changeStateOnRemoteUpdate(){
		throw new NotImplException(action.getCurrentState().getStateType().getString() + ".changeStateOnRemoteUpdate");
	}

	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath){
		throw new NotImplException(action.getCurrentState().getStateType().getString() + ".changeStateOnRemoteMove");
	}
	
	/*
	 * LOCAL event handler
	 */
	
	public abstract AbstractActionState handleLocalCreate();
	
	public AbstractActionState handleLocalHardDelete(){
		logger.trace("File {}: entered handleLocalHardDelete", action.getFilePath());
		updateTimeAndQueue();
		return changeStateOnLocalHardDelete();
	}
	
	public AbstractActionState handleLocalDelete(){
		logger.trace("File {}: entered handleLocalDelete", action.getFilePath());
		IFileEventManager eventManager = action.getEventManager();
		eventManager.getFileComponentQueue().remove(action.getFile());
		if(action.getFile().isFile()){
			String oldHash = action.getFile().getContentHash();
//			action.getFile().updateContentHash();
			logger.debug("File: {}Previous content hash: {} new content hash: ", action.getFilePath(), oldHash, action.getFile().getContentHash());
			SetMultimap<String, FileComponent> deletedFiles = action.getEventManager().getFileTree().getDeletedByContentHash();
			deletedFiles.put(action.getFile().getContentHash(), action.getFile());
			logger.debug("Put deleted file {} with hash {} to SetMultimap<String, FileComponent>", action.getFilePath(), action.getFile().getContentHash());
		} else {

			Map<String, FolderComposite> deletedFolders = eventManager.getFileTree().getDeletedByContentNamesHash();
			logger.debug("Added folder {} with structure hash {} to deleted folders.", action.getFilePath(), action.getFile().getStructureHash());
			deletedFolders.put(action.getFile().getStructureHash(), (FolderComposite)action.getFile());
		}
//		FileComponent comp = eventManager.getFileTree().deleteComponent(action.getFile().getPath().toString());
//		logger.debug("After delete hash of {} is {}", comp.getPath(), comp.getStructureHash());

		//		eventManager.getFileComponentQueue().add(action.getFile());
		updateTimeAndQueue();
		return changeStateOnLocalDelete();
	}
	
	public AbstractActionState handleLocalUpdate(){
		return changeStateOnLocalUpdate();
	}
	
	public AbstractActionState handleLocalMove(Path oldFilePath){
		return changeStateOnLocalMove(oldFilePath);
	}

	public AbstractActionState handleLocalRecover(int version){
		return changeStateOnLocalRecover(version);
	}
	
	/*
	 * REMOTE event handler
	 */
	
	public AbstractActionState handleRemoteCreate(){
		return changeStateOnRemoteCreate();
	}
	
	public AbstractActionState handleRemoteDelete(){
		return changeStateOnRemoteDelete();
	}
	
	public AbstractActionState handleRemoteUpdate(){
		return changeStateOnRemoteUpdate();
	}
	
	public AbstractActionState handleRemoteMove(Path path){
		return changeStateOnRemoteMove(path);
	}
	
	/*
	 * Execution and notification related functions
	 */

	public abstract ExecutionHandle execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException;
	
	
	protected void notifyActionExecuteSucceeded() {
		//action.setIsExecuting(false);
		Set<IActionEventListener> listener = 
				new HashSet<IActionEventListener>(action.getEventListener());
		Iterator<IActionEventListener> it = listener.iterator();
		while(it.hasNext()) {
			it.next().onActionExecuteSucceeded(action);
		}
	}
	

	protected void notifyActionExecuteFailed() {
		//action.setIsExecuting(false);
		Set<IActionEventListener> listener = 
				new HashSet<IActionEventListener>(action.getEventListener());
		Iterator<IActionEventListener> it = listener.iterator();
		while(it.hasNext()) {
			it.next().onActionExecuteFailed(action, handle);
		}
	}
	
	protected class FileManagerProcessListener implements IProcessComponentListener {
		
		public FileManagerProcessListener() {
			
		}

		@Override
		public void onExecuting(IProcessEventArgs args) {
			
		}

		@Override
		public void onRollbacking(IProcessEventArgs args) {
			System.out.println("Rollback started!");
		}

		@Override
		public void onPaused(IProcessEventArgs args) {
			
		}

		@Override
		public void onExecutionSucceeded(IProcessEventArgs args) {
			notifyActionExecuteSucceeded();
		}

		@Override
		public void onExecutionFailed(IProcessEventArgs args) {
			System.out.println("Execution failed!");
			notifyActionExecuteFailed();
		}


		@Override
		public void onRollbackSucceeded(IProcessEventArgs args) {
			
		}

		@Override
		public void onRollbackFailed(IProcessEventArgs args) {
			
		}
	}
}

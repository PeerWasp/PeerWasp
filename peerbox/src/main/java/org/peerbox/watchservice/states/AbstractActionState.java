package org.peerbox.watchservice.states;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.FileManager;
import org.peerbox.h2h.ProcessHandle;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.FileComponent;
import org.peerbox.watchservice.FolderComposite;
import org.peerbox.watchservice.IActionEventListener;
import org.peerbox.watchservice.IFileEventManager;
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

	public AbstractActionState(Action action) {
		this.action = action;
	}
	
	public AbstractActionState getDefaultState(){
		logger.debug("Return to default state 'EstablishedState': {}", action.getFile().getPath());
		return new EstablishedState(action);
	}
	
	public void updateTimeAndQueue(){
		action.getEventManager().getFileComponentQueue().remove(action.getFile());
		action.updateTimestamp();
		action.getEventManager().getFileComponentQueue().add(action.getFile());
	}

	/*
	 * LOCAL state changers
	 */
	public abstract AbstractActionState changeStateOnLocalCreate();

	public abstract AbstractActionState changeStateOnLocalDelete();

	public abstract AbstractActionState changeStateOnLocalUpdate();

	public abstract AbstractActionState changeStateOnLocalMove(Path oldPath);
	
	public AbstractActionState changeStateOnLocalHardDelete(){
		return new LocalHardDeleteState(action);
	}

	/*
	 * REMOTE state changers
	 */
	public abstract AbstractActionState changeStateOnRemoteDelete();
	
	public abstract AbstractActionState changeStateOnRemoteCreate();

	public abstract AbstractActionState changeStateOnRemoteUpdate();

	public abstract AbstractActionState changeStateOnRemoteMove(Path oldFilePath);
	
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
			SetMultimap<String, FileComponent> deletedFiles = action.getEventManager().getDeletedFileComponents();
			deletedFiles.put(action.getFile().getContentHash(), action.getFile());
			logger.debug("Put deleted file {} with hash {} to SetMultimap<String, FileComponent>", action.getFilePath(), action.getFile().getContentHash());
		} else {

			Map<String, FolderComposite> deletedFolders = eventManager.getDeletedByContentNamesHash();
			logger.debug("Added folder {} with structure hash {} to deleted folders.", action.getFilePath(), action.getFile().getStructureHash());
			deletedFolders.put(action.getFile().getStructureHash(), (FolderComposite)action.getFile());
		}
		FileComponent comp = eventManager.getFileTree().deleteComponent(action.getFile().getPath().toString());
		logger.debug("After delete hash of {} is {}", comp.getPath(), comp.getStructureHash());
//		eventManager.getFileComponentQueue().add(action.getFile());
		updateTimeAndQueue();
		return changeStateOnLocalDelete();
	}
	
	public abstract AbstractActionState handleLocalUpdate();
	
	public abstract AbstractActionState handleLocalMove(Path oldFilePath);
	
	/*
	 * REMOTE event handler
	 */
	
	public abstract AbstractActionState handleRemoteCreate();
	
	public abstract AbstractActionState handleRemoteDelete();
	
	public abstract AbstractActionState handleRemoteUpdate();
	
	public abstract AbstractActionState handleRemoteMove(Path path);
	
	/*
	 * Execution and notification related functions
	 */

	public abstract void execute(FileManager fileManager) throws NoSessionException,
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

	protected void notifyActionExecuteFailed(ProcessExecutionException pex) {
		//action.setIsExecuting(false);
		Set<IActionEventListener> listener = 
				new HashSet<IActionEventListener>(action.getEventListener());
		Iterator<IActionEventListener> it = listener.iterator();
		while(it.hasNext()) {
			it.next().onActionExecuteFailed(action, pex);
		}
	}	
	
	protected class FileManagerProcessListener implements IProcessComponentListener {
		private final ProcessHandle<Void> asyncHandle;
		
		public FileManagerProcessListener(ProcessHandle<Void> handle) {
			this.asyncHandle = handle;
		}

		@Override
		public void onExecuting(IProcessEventArgs args) {
			
		}

		@Override
		public void onRollbacking(IProcessEventArgs args) {
			
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
			ProcessExecutionException pex = null;
			try {
				asyncHandle.getFuture().get();
			} catch(InterruptedException iex) {
				
			} catch(ExecutionException | CancellationException ex) {
				if(ex.getCause() instanceof ProcessExecutionException) {
					pex = (ProcessExecutionException) ex.getCause();
				}
			}
			notifyActionExecuteFailed(pex);
		}

		@Override
		public void onRollbackSucceeded(IProcessEventArgs args) {
			
		}

		@Override
		public void onRollbackFailed(IProcessEventArgs args) {
			
		}
	}
}

package org.peerbox.watchservice.states;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.peerbox.FileManager;
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
	
	public abstract AbstractActionState changeStateOnLocalRecover(int versionToRecover);

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
	
	public AbstractActionState handleLocalDelete(){
		IFileEventManager eventManager = action.getEventManager();
		eventManager.getFileComponentQueue().remove(action.getFile());
		if(action.getFile().isFile()){
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
		eventManager.getFileComponentQueue().add(action.getFile());
		return changeStateOnLocalDelete();
	}
	
	public abstract AbstractActionState handleLocalUpdate();
	
	public abstract AbstractActionState handleLocalMove(Path oldFilePath);
	
	public abstract AbstractActionState handleLocalRecover(int version);
	
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
			NoPeerConnectionException, IllegalFileLocation, InvalidProcessStateException;
	
	
	protected void notifyActionExecuteSucceeded() {
		Set<IActionEventListener> listener = 
				new HashSet<IActionEventListener>(action.getEventListener());
		Iterator<IActionEventListener> it = listener.iterator();
		while(it.hasNext()) {
			it.next().onActionExecuteSucceeded(action);
		}
	}

	protected void notifyActionExecuteFailed(RollbackReason reason) {
		Set<IActionEventListener> listener = 
				new HashSet<IActionEventListener>(action.getEventListener());
		Iterator<IActionEventListener> it = listener.iterator();
		while(it.hasNext()) {
			it.next().onActionExecuteFailed(action, reason);
		}
	}	
	
	protected class FileManagerProcessListener implements IProcessComponentListener {
		@Override
		public void onSucceeded() {
			notifyActionExecuteSucceeded();
		}

		@Override
		public void onFailed(RollbackReason reason) {
			notifyActionExecuteFailed(reason);
		}
	}
}

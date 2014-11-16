package org.peerbox.watchservice.states;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.IActionEventListener;

/**
 * Interface for different states of implemented state pattern
 * 
 * @author winzenried
 *
 */
public abstract class AbstractActionState {

	protected Action action;

	public AbstractActionState(Action action) {
		this.action = action;
	}
	
	public AbstractActionState getDefaultState(){
		return new InitialState(action);
	}

	/*
	 * LOCAL event handlers
	 */
	public abstract AbstractActionState handleLocalCreateEvent();

	public abstract AbstractActionState handleLocalDeleteEvent();

	public abstract AbstractActionState handleLocalUpdateEvent();

	public abstract AbstractActionState handleLocalMoveEvent(Path oldFilePath);
	
	public abstract AbstractActionState handleRecoverEvent(int versionToRecover);

	/*
	 * REMOTE event handlers
	 */
	public abstract AbstractActionState handleRemoteDeleteEvent();
	
	public abstract AbstractActionState handleRemoteCreateEvent();

	public abstract AbstractActionState handleRemoteUpdateEvent();

	public abstract AbstractActionState handleRemoteMoveEvent(Path oldFilePath);

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

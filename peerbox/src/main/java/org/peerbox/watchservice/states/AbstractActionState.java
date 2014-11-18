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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		logger.debug("Type {} here", this.getClass());
		return new EstablishedState(action);
	}

	/*
	 * LOCAL state changers
	 */
	public abstract AbstractActionState changeStateOnLocalCreate();

	public abstract AbstractActionState changeStateOnLocalDelete();

	public abstract AbstractActionState changeStateOnLocalUpdate();

	public abstract AbstractActionState changeStateOnLocalMove(Path oldFilePath);
	
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
	
	public abstract void handleLocalCreate();
	
	public abstract void handleLocalDelete();
	
	public abstract void handleLocalUpdate();
	
	public abstract void handleLocalMove();
	
	public abstract void handleLocalRecover();
	
	/*
	 * REMOTE event handler
	 */
	
	public abstract void handleRemoteCreate();
	
	public abstract void handleRemoteDelete();
	
	public abstract void handleRemoteUpdate();
	
	public abstract void handleRemoteMove();
	
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

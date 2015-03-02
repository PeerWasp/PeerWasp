package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.peerbox.watchservice.states.AbstractActionState;
import org.peerbox.watchservice.states.EstablishedState;
import org.peerbox.watchservice.states.ExecutionHandle;
import org.peerbox.watchservice.states.InitialState;
import org.peerbox.watchservice.states.LocalMoveState;
import org.peerbox.watchservice.states.RemoteUpdateState;
import org.peerbox.watchservice.states.StateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Action class has the context role in the State Pattern used to implement
 * the state machine. It provides a systematic and lose-coupled way to change the
 * state of a {@link org.peerbox.watchservice.filetree.composite.FileComponent 
 * FileComponent}. In general, there is exactly one instance of each {@link org.peerbox.
 * watchservice.filetree.composite.FileComponent FileComponent} and Action which form
 * a tightly coupled pair. While the {@link org.peerbox.watchservice.filetree.
 * composite.FileComponent FileComponent} is primarily used to represent files
 * and their properties, Action is used to represent the application internal state 
 * and to define the upcoming network operation for files.
 *
 *
 * @author albrecht, anliker, winzenried
 *
 */

public class Action implements IAction {

	private final static Logger logger = LoggerFactory.getLogger(Action.class);

	/**
	 * An instance of {@link org.peerbox.watchservice.filetree.composite.
	 * FileComponent FileComponent} which is tightly coupled with this 
	 * Action. This object represents the file in the PeerWasp system for
	 * which this Action object maintains the state.
	 */
	private FileComponent file;
	
	private final AtomicLong timestamp;

	/**
	 * This state defines what operations are performed if the Action is executed
	 * in the future. Events are applied to this state if the Action is not executed
	 * when the event occurs.
	 */
	private volatile AbstractActionState currentState;
	
	private volatile AbstractActionState nextState;

	/** 
	 * Is true if the Action is currenly executed, i.e. after {@link #execute(IFileManager)} 
	 * is called and before {@link #onSucceeded()} or {@link #onFailed()} are called.
	 */
	private volatile boolean isExecuting = false;
	
	private volatile boolean changedWhileExecuted = false;
	
	/**
	 * How many times the PeerWasp tried to perform the current execution.
	 * If the Action is currently not executed, this is 0.
	 */
	private volatile int executionAttempts = 0;

	/**
	 * This dependency is important as most states need this instance in various places
	 * to perform a correct event handling.
	 */
	private IFileEventManager fileEventManager;

	private final Lock lock = new ReentrantLock();

	public Action() {
		this(null);
	}

	/**
	 * A new action is always in the {@link org.peerbox.watchservice.states.InitialState
	 * InitialState}.
	 * @param fileEventManager used to provide for 
	 */
	public Action(final IFileEventManager fileEventManager) {
		this.currentState = new InitialState(this);
		this.nextState = new EstablishedState(this);
		timestamp = new AtomicLong(Long.MAX_VALUE);
		this.fileEventManager = fileEventManager;
		updateTimestamp();
	}

	/**
	 * Handles the local create event. If {@link isExecuting} == false,
	 * the event is handled on the currentState. If it is true, the nextState
	 * is changed accordingly and the {@link changedWhileExecuted} flag is set
	 * if needed.
	 */
	@Override
	public void handleLocalCreateEvent() {
		logger.trace("handleLocalCreateEvent - File: {}, isExecuting({})",
				getFile().getPath(), isExecuting());
		try {
			acquireLock();

			if (isExecuting()) {
				nextState = nextState.changeStateOnLocalCreate();
				checkIfChanged();

			} else {
				updateTimestamp();
				currentState = currentState.handleLocalCreate();
				nextState = currentState.getDefaultState();
			}

		} finally {
			releaseLock();
		}
	}

	/**
	 * Handles the local update event. For further documentation on event handling, 
	 * check {@link #handleLocalCreateEvent()}. If currentState is of 
	 * type {@link org.peerbox.watchservice.states.RemoteUpdateState 
	 * RemoteUpdateState} and {@link org.peerbox.watchservice.states.
	 * RemoteUpdateState#getLocalUpdateHappened() RemoteUpdateState.getLocal
	 * UpdateHappened()} == false (i.e. no local update event happened yet), the
	 * event is ignored. This is useful, as the local update event introduced by the
	 * H2H download() API call should not be mistaken for an actual local file change.
	 * In any other case, the local update is simply forwarded to the corresponding
	 * state.
	 */
	@Override
	public void handleLocalUpdateEvent() {
		logger.trace("handleLocalUpdateEvent - File: {}, isExecuting({})",
				getFile().getPath(), isExecuting());
		try {
			acquireLock();

			if (isExecuting()) {
				if(currentState.getStateType() == StateType.REMOTE_CREATE){
//					RemoteCreateState castedState = (RemoteCreateState)currentState;
//					if(!castedState.localCreateHappened()){
//						nextState = nextState.changeStateOnLocalUpdate();
//						checkIfChanged();
//					} else {
//						logger.debug("File {}: LocalUpdateEvent after LocalCreateEvent "
//								+ "in RemoteCreateState - ignored!", file.getPath());
//					}
				} else if(currentState.getStateType() == StateType.REMOTE_UPDATE){
					RemoteUpdateState castedState = (RemoteUpdateState)currentState;
					if(castedState.getLocalUpdateHappened()){
						nextState = nextState.changeStateOnLocalUpdate();
						checkIfChanged();
					} else {
						castedState.setLocalUpdateHappened(true);
						logger.debug("File {}: First LocalUpdateEvent "
								+ "in RemoteUpdateState - ignored!", file.getPath());
					}
				} else {
					nextState = nextState.changeStateOnLocalUpdate();
					checkIfChanged();
				}


			} else {

				updateTimestamp();
				if (currentState instanceof LocalMoveState) {
					nextState = nextState.changeStateOnLocalUpdate();
				} else {
					currentState = currentState.handleLocalUpdate();
					nextState = currentState.getDefaultState();
				}

			}

		} finally {
			releaseLock();
		}
	}

	/**
	 * Handles the local delete event triggered when a file is removed from the HDD
	 * (or moved into the trash bin). For further documentation on event handling, 
	 * check {@link #handleLocalCreateEvent()}.
	 */
	@Override
	public void handleLocalDeleteEvent() {
		logger.trace("handleLocalDeleteEvent - File: {}, isExecuting({})",
				getFile().getPath(), isExecuting());
		try {
			acquireLock();

			if (isExecuting()) {

				nextState = nextState.changeStateOnLocalDelete();
				checkIfChanged();

			} else {

				updateTimestamp();
				currentState = currentState.handleLocalDelete();
				nextState = currentState.getDefaultState();

			}

		} finally {
			releaseLock();
		}
	}

	/**
	 * Handles the local hard delete event. This event is triggered by the PeerWasp 
	 * code and not by the file system. If the connected {@link org.peerbox.watchservice.
	 * filetree.composite.FileComponent FileComponent} is a {@link org.peerbox.watchservice.
	 * filetree.composite.FolderComposite FolderComposite}, the event is propagated manually
	 * to all children to initiate a recursive handling. Besides that, the file is removed from
	 * the HDD by code. This triggers a local delete event. For further documentation on 
	 * event handling, check {@link #handleLocalCreateEvent()}.
	 */
	@Override
	public void handleLocalHardDeleteEvent(){
		logger.trace("handleLocalHardDeleteEvent - File: {}, isExecuting({})",
				getFile().getPath(), isExecuting());

		if (getFile().isFolder()) {
			logger.trace("Folder {} - delete children", getFile().getPath());
			FolderComposite folder = (FolderComposite) getFile();
			Map<Path, FileComponent> children = folder.getChildren();

			for (Map.Entry<Path, FileComponent> childEntry : children.entrySet()) {
				FileComponent child = childEntry.getValue();
				child.getAction().handleLocalHardDeleteEvent();
			}
		}

		try {
			acquireLock();

			if (isExecuting()) {

				nextState = nextState.changeStateOnLocalHardDelete();
				checkIfChanged();

			} else {

				updateTimestamp();
				currentState = currentState.handleLocalHardDelete();
				nextState = currentState.getDefaultState();

			}

			try {
				if (!Files.exists(getFile().getPath())) {
					return;
				}
				Files.delete(getFile().getPath());
				logger.trace("DELETED FROM DISK: {}", getFile().getPath());
			} catch (IOException e) {
				logger.warn("Could not delete file: {} ({})",
						getFile().getPath(), e.getMessage(), e);
			}

		} finally {
			releaseLock();
		}
	}

	/**
	 * Handles the local move event. This event is triggered by the PeerWasp
	 * code when a local delete and a local create event correspond to a
	 * file move performed by the user.
	 * 
	 * In general, file modifications lead to modify events. Occasionally, the
	 * file system triggers an additional pair of delete/create events on the same 
	 * file. This case is inadvertently interpreted as a move to the same location 
	 * up to this point. Hence, the handling of such an event stops here.
	 * 
	 * For further documentation on event handling, 
	 * check {@link #handleLocalCreateEvent()}.
	 */
	@Override
	public void handleLocalMoveEvent(Path oldFilePath) {
		logger.debug("handleLocalMoveEvent - File: {}, isExecuting({})",
				getFile().getPath(), isExecuting());
		try {
			acquireLock();

			if (isExecuting()) {

				nextState = nextState.changeStateOnLocalMove(oldFilePath);
				checkIfChanged();

			} else {

				updateTimestamp();

				if (oldFilePath.equals(getFile().getPath())) {
					logger.trace("File {}: Move to same location due to update!",
							getFile().getPath());
					fileEventManager.getFileTree().getDeletedByContentHash()
							.get(getFile().getContentHash()).remove(oldFilePath);
					return;
				}

				currentState = currentState.handleLocalMove(oldFilePath);
				nextState = currentState.getDefaultState();

			}
		} finally {
			releaseLock();
		}
	}

	/**
	 * Handles the remote create event triggered by Hive2Hive. For further documentation 
	 * on event handling, check {@link #handleLocalCreateEvent()}.
	 */
	@Override
	public void handleRemoteCreateEvent() {
		logger.trace("handleRemoteCreateEvent - File: {}, isExecuting({})",
				getFile().getPath(), isExecuting());
		try {
			acquireLock();

			if (isExecuting()) {

				nextState = nextState.changeStateOnRemoteCreate();
				checkIfChanged();

			} else {

				updateTimestamp();
				currentState = currentState.handleRemoteCreate();
				nextState = currentState.getDefaultState();

			}

		} finally {
			releaseLock();
		}
	}

	/**
	 * Handles the remote update event triggered by Hive2Hive. For further documentation 
	 * on event handling, check {@link #handleLocalCreateEvent()}.
	 */
	@Override
	public void handleRemoteUpdateEvent() {
		logger.trace("handleRemoteUpdateEvent - File: {}, isExecuting({})",
				getFile().getPath(), isExecuting());
		try {
			acquireLock();

			if (isExecuting()) {

				nextState = nextState.changeStateOnRemoteUpdate();
				checkIfChanged();

			} else {

				updateTimestamp();
				currentState = currentState.handleRemoteUpdate();
				nextState = currentState.getDefaultState();

			}

		} finally {
			releaseLock();
		}
	}

	/**
	 * Handles the remote delete event triggered by Hive2Hive. For further documentation 
	 * on event handling, check {@link #handleLocalCreateEvent()}. If the connected 
	 * {@link org.peerbox.watchservice. filetree.composite.FileComponent FileComponent} 
	 * is a {@link org.peerbox.watchservice.filetree.composite.FolderComposite FolderComposite},
	 * the event is propagated manually to all children to initiate a recursive handling. 
	 * For further documentation on event handling, check {@link #handleLocalCreateEvent()}.
	 */
	@Override
	public void handleRemoteDeleteEvent() {
		logger.trace("handleRemoteDeleteEvent - File: {}, isExecuting({})",
				getFile().getPath(), isExecuting());

		if (getFile().isFolder()) {
			logger.trace("Folder {} - delete children", getFile().getPath());
			FolderComposite folder = (FolderComposite) getFile();
			Map<Path, FileComponent> children = folder.getChildren();

			for (Map.Entry<Path, FileComponent> childEntry : children.entrySet()) {
				FileComponent child = childEntry.getValue();
				child.getAction().handleRemoteDeleteEvent();
			}
		}

		try {
			acquireLock();

			if (isExecuting()) {

				nextState = nextState.changeStateOnRemoteDelete();
				checkIfChanged();

			} else {

				updateTimestamp();
				currentState = currentState.handleRemoteDelete();
				nextState = currentState.getDefaultState();

			}

		} finally {
			releaseLock();
		}
	}

	/**
	 * Handles the remote move event triggered by Hive2Hive. This method moves the 
	 * file in the file system. For further documentation on event handling, 
	 * check {@link #handleLocalCreateEvent()}.
	 */
	@Override
	public void handleRemoteMoveEvent(Path path) {
		logger.trace("handleRemoteMoveEvent - File: {}, isExecuting({})",
				getFile().getPath(), isExecuting());

		try {
			acquireLock();

			Path srcPath = getFile().getPath();
			if (isExecuting()) {

				nextState = nextState.changeStateOnRemoteMove(path);
				checkIfChanged();

			} else {

				updateTimestamp();
				currentState = currentState.handleRemoteMove(path);
				nextState = currentState.getDefaultState();

				try {
					if (!Files.exists(srcPath)) {
						return;
					}
					Files.move(srcPath, path);
				} catch (IOException e) {
					logger.warn("Could not move file: from src={} to dst={} ({})",
							srcPath, path, e.getMessage(), e);
				}

			}

		} finally {
			releaseLock();
		}
	}

	private void checkIfChanged() {
		if (!(nextState instanceof EstablishedState)) {
			logger.trace("File {}: Next state is {}, keep track of change",
					getFile().getPath(), getNextStateName());
			changedWhileExecuted = true;
		} else {
			logger.trace("File {}: Next state is {}, no change detected",
					getFile().getPath(), getNextStateName());
		}
	}

	/**
	 * Each state is able to execute an action as soon the state is considered as stable.
	 * The action itself depends on the current state (e.g. add file, delete file, etc.).
	 * If an action does not execute anything (i.e. if the state is {@link org.peerbox.
	 * watchservice.states.InitialState InitialState} or {@link org.peerbox.
	 * watchservice.states.EstablishedState EstablishedState}, the returned handle is null.
	 * As a consequence, the {@link #isExecuting} flag is set to false in this case.
	 * @return
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 * @throws IllegalFileLocation
	 * @throws InvalidProcessStateException
	 * @throws ProcessExecutionException
	 */
	@Override
	public ExecutionHandle execute(IFileManager fileManager)
			throws NoSessionException, NoPeerConnectionException,
			InvalidProcessStateException, ProcessExecutionException {

		if (isExecuting()) {
			throw new IllegalStateException("Action is already executing.");
		}
		ExecutionHandle ehandle = null;
		try {
			acquireLock();


			setIsExecuting(true);
			++executionAttempts;
			ehandle = currentState.execute(fileManager);
			if(ehandle == null){
				setIsExecuting(false);
			}
			return ehandle;

//		} catch(IllegalArgumentException ex){
//			logger.trace("Captured IllegalArgumentException ex for {}", getFile().getPath());
//			setIsExecuting(false);
//			return null;
		} finally {
			releaseLock();
		}

	}

	/**
	 * This method performs the cleanup routine after an action's execution
	 * terminated successfully as expected. It performs a state transition from
	 * the {@link #currentState} to the {@link #nextState}, sets the {@link
	 * #isExecuting} flag to false, and resets the {@link #changedWhileExecuted} flag
	 * to false as well. To prevent concurrent manipulations on
	 * the Action object, the object is locked in the meantime.
	 */
	@Override
	public void onSucceeded() {
		logger.trace("onSucceeded: File {} - Switch state from {} to {}",
				getFile().getPath(), getCurrentStateName(), getNextStateName());
		try {
			acquireLock();

			currentState = nextState;
			nextState = nextState.getDefaultState();
			setIsExecuting(false);
			changedWhileExecuted = false;
			executionAttempts = 0;

		} finally {
			releaseLock();
		}
	}

	/**
	 * This method performs the cleanup routine after an action's execution
	 * terminated but failed. Until know, it only sets the {@link
	 * #isExecuting} flag to false. To prevent concurrent manipulations on
	 * the Action object, the object is locked in the meantime.
	 */
	@Override
	public void onFailed() {
		try {
			acquireLock();
			setIsExecuting(false);
		} finally {
			releaseLock();
		}
	}

	private void acquireLock() {
		logger.trace("File {}: Wait for own lock at t={} in State {}",
				getFile().getPath(), System.currentTimeMillis(), getCurrentState().getStateType());
		lock.lock();
		logger.trace("File {}: Received own lock at t={} in State {}",
				getFile().getPath(), System.currentTimeMillis(), getCurrentState().getStateType());
	}

	private void releaseLock() {
		lock.unlock();
		logger.trace("File {}: Released own lock at t={} in State {}",
				getFile().getPath(), System.currentTimeMillis(), getCurrentState().getStateType());
	}


	/**
	 * @return The {@link #currentState}
	 */
	@Override
	public AbstractActionState getCurrentState() {
		return currentState;
	}

	/**
	 * @return The simple name of the state (e.g. "LocalCreateState")
	 */
	@Override
	public String getCurrentStateName() {
		return currentState != null ? currentState.getClass().getSimpleName() : "null";
	}

	/**
	 * @return The {@link #nextState}
	 */
	@Override
	public AbstractActionState getNextState() {
		return nextState;
	}

	/**
	 * @return The simple name of the {@link #nextState} (e.g. "LocalCreateState")
	 */
	@Override
	public String getNextStateName() {
		return nextState != null ? nextState.getClass().getSimpleName() : "null";
	}

	/**
	 * @return True if events happened during the Action's execution which changed the Action
	 * in a way that implies further network operations. Example: While a local
	 * create event is executed, the file is already modified locally.
	 */
	@Override
	public boolean getChangedWhileExecuted() {
		return changedWhileExecuted;
	}

	/**
	 * @return How many times the PeerWasp tried to perform the current execution.
	 */
	@Override
	public int getExecutionAttempts() {
		return executionAttempts;
	}

	/**
	 * @return Returns the flag {@link isExecuting}.
	 */
	@Override
	public boolean isExecuting() {
		return isExecuting;
	}

	private void setIsExecuting(final boolean isExecuting) {
		this.isExecuting = isExecuting;
	}

	/**
	 * @returns The {@link #timeStamp}.
	 */
	@Override
	public long getTimestamp() {
		return timestamp.get();
	}

	/**
	 * Updates the {@link timestamp} of this Action object to the current time
	 * in milliseconds.
	 */
	@Override
	public void updateTimestamp() {
		timestamp.set(System.currentTimeMillis());
	}

	/**
	 * @returns The {@link #fileEventManager}.
	 */
	@Override
	public IFileEventManager getFileEventManager() {
		return fileEventManager;
	}

	/**
	 * Sets the {@link #fileEventManager}.
	 */
	@Override
	public void setFileEventManager(final IFileEventManager fileEventManager) {
		this.fileEventManager = fileEventManager;
	}

	/**
	 * @returns The {@link #file}.
	 */
	@Override
	public FileComponent getFile() {
		return file;
	}

	/**
	 * Sets the {@link #file}.
	 */
	@Override
	public void setFile(final FileComponent file) {
		this.file = file;
	}

	/**
	 * Sets the {@link #currentState}. This should never be
	 * used change the Action's state manually due to file events,
	 * as the state machine already sets the states accordingly!
	 */
	@Override
	public void setCurrentState(AbstractActionState state) {
		this.currentState = state;
	}

	/**
	 * Prints the Action object using a specified format. Example:
	 * "Action[currentState(LocalCreateState), nextState(EstablishedState), 
	 * isExecuting(true), changedWhileExecuted(false), executionAttempts(2)".
	 */
	@Override
	public String toString() {
		return String.format("Action[currentState(%s), nextState(%s), isExecuting(%s), changedWhileExecuted(%s), executionAttempts(%d),]",
				getCurrentStateName(), getNextStateName(), isExecuting(), changedWhileExecuted, executionAttempts);
	}

}

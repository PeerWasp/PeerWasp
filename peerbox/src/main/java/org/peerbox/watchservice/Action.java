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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Action class provides a systematic and lose-coupled way to change the
 * state of an object as part of the chosen state pattern design.
 *
 *
 * @author albrecht, anliker, winzenried
 *
 */

public class Action implements IAction {

	private final static Logger logger = LoggerFactory.getLogger(Action.class);

	private FileComponent file;
	private final AtomicLong timestamp;

	private volatile AbstractActionState currentState;
	private volatile AbstractActionState nextState;

	private volatile boolean isExecuting = false;
	private volatile boolean changedWhileExecuted = false;
	private volatile int executionAttempts = 0;

	private IFileEventManager fileEventManager;

	private final Lock lock = new ReentrantLock();

	public Action() {
		this(null);
	}

	/**
	 * Initialize with timestamp and set currentState to initial state
	 */
	public Action(final IFileEventManager fileEventManager) {
		this.currentState = new InitialState(this);
		this.nextState = new EstablishedState(this);
		timestamp = new AtomicLong(Long.MAX_VALUE);
		this.fileEventManager = fileEventManager;
		updateTimestamp();
	}

	/**
	 * changes the state of the currentState to Create state if current state allows it.
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
	 * changes the state of the currentState to Modify state if current state allows it.
	 */
	@Override
	public void handleLocalUpdateEvent() {
		logger.trace("handleLocalUpdateEvent - File: {}, isExecuting({})",
				getFile().getPath(), isExecuting());
		try {
			acquireLock();

			if (isExecuting()) {

				nextState = nextState.changeStateOnLocalUpdate();
				checkIfChanged();

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
	 * changes the state of the currentState to Delete state if current state allows it.
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
	 * The action itself depends on the current state (e.g. add file, delete file, etc.)
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

		try {
			acquireLock();

			ExecutionHandle ehandle = null;
			setIsExecuting(true);
			++executionAttempts;
			ehandle = currentState.execute(fileManager);
			return ehandle;

		} finally {
			releaseLock();
		}

	}

	@Override
	public void onSucceeded() {
		logger.trace("onSucceeded: File {} - Switch state from {} to {}",
				getFile().getPath(), getCurrentStateName(), getNextStateName());
		try {
			acquireLock();

			currentState.performCleanup();
			currentState = nextState;
			nextState = nextState.getDefaultState();
			setIsExecuting(false);
			changedWhileExecuted = false;
			executionAttempts = 0;

		} finally {
			releaseLock();
		}
	}

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
	 * @return current state object
	 */
	@Override
	public AbstractActionState getCurrentState() {
		return currentState;
	}

	@Override
	public String getCurrentStateName() {
		return currentState != null ? currentState.getClass().getSimpleName() : "null";
	}

	@Override
	public AbstractActionState getNextState() {
		return nextState;
	}

	@Override
	public String getNextStateName() {
		return nextState != null ? nextState.getClass().getSimpleName() : "null";
	}

	@Override
	public boolean getChangedWhileExecuted() {
		return changedWhileExecuted;
	}

	@Override
	public int getExecutionAttempts() {
		return executionAttempts;
	}

	@Override
	public boolean isExecuting() {
		logger.trace("File {} is executing: {}", file.getPath(), isExecuting);
		return isExecuting;
	}

	private void setIsExecuting(final boolean isExecuting) {
		this.isExecuting = isExecuting;
	}

	@Override
	public long getTimestamp() {
		return timestamp.get();
	}

	@Override
	public void updateTimestamp() {
		timestamp.set(System.currentTimeMillis());
	}

	@Override
	public IFileEventManager getFileEventManager() {
		return fileEventManager;
	}

	@Override
	public void setFileEventManager(final IFileEventManager fileEventManager) {
		this.fileEventManager = fileEventManager;
	}

	@Override
	public FileComponent getFile() {
		return file;
	}

	@Override
	public void setFile(final FileComponent file) {
		this.file = file;
	}

}

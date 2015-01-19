package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
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

public class Action implements IAction{
	private final static Logger logger = LoggerFactory.getLogger(Action.class);
	private long timestamp = Long.MAX_VALUE;

	private AbstractActionState currentState;
	private AbstractActionState nextState;
	private Set<IActionEventListener> eventListeners;
	private int executionAttempts = 0;
	private IFileEventManager eventManager;
	private FileComponent file;

	private boolean isExecuting = false;
	private boolean	changedWhileExecuted = false;
	private final Lock lock = new ReentrantLock();

	/**
	 * Initialize with timestamp and set currentState to initial state
	 */
	public Action(IFileEventManager fileEventManager){
		currentState = new InitialState(this);
		nextState = new EstablishedState(this);
		eventListeners = new HashSet<IActionEventListener>();
		this.eventManager = fileEventManager;
		updateTimestamp();
	}

	public Action(){
		this(null);
	}

	public void setEventManager(IFileEventManager fileEventManager){
		this.eventManager = fileEventManager;
	}

	public IFileEventManager getEventManager(){
		return eventManager;
	}

	public FileComponent getFile() {
		return file;
	}

	public void setFile(FileComponent file) {
		this.file = file;
	}

	public void updateTimestamp() {
		timestamp = System.currentTimeMillis();
	}

	/**
	 * changes the state of the currentState to Create state if current state allows it.
	 */
	public void handleLocalCreateEvent(){
		logger.trace("handleLocalCreateEvent - File: {}", getFile().getPath());
		if(isExecuting){

			acquireLockOnThis();
			logger.trace("Event occured for {} while executing.", getFile().getPath());
			nextState = nextState.changeStateOnLocalCreate();
			checkIfChanged();
			releaseLockOnThis();
		} else {
			acquireLockOnThis();
			updateTimestamp();

			currentState = currentState.handleLocalCreate();
			nextState = currentState.getDefaultState();
			releaseLockOnThis();
		}
	}

	private void releaseLockOnThis() {
		logger.trace("File {}: Release lock on this at {}", getFile().getPath(), System.currentTimeMillis());
		lock.unlock();
	}

	private void acquireLockOnThis() {
			logger.trace("File {}: Wait for own lock at {}", getFile().getPath(), System.currentTimeMillis());
			lock.lock();
			logger.trace("File {}: Received own lock at {}", getFile().getPath(), System.currentTimeMillis());
	}

	/**
	 * changes the state of the currentState to Modify state if current state allows it.
	 */
	public void handleLocalUpdateEvent(){
		logger.trace("handleLocalUpdateEvent - File: {}", getFile().getPath());
		acquireLockOnThis();
		if(isExecuting){


			logger.trace("Event occured for {} while executing.", getFile().getPath());
			nextState = nextState.changeStateOnLocalUpdate();
			checkIfChanged();

			logger.trace("Set next state for {} to {}", getFile().getPath(), nextState.getClass());
		} else {
			updateTimestamp();
			if(currentState instanceof LocalMoveState){

				nextState = nextState.changeStateOnLocalUpdate();
			} else {
				currentState = currentState.handleLocalUpdate();
				nextState = currentState.getDefaultState();
			}
		}
		releaseLockOnThis();
	}

	/**
	 * changes the state of the currentState to Delete state if current state allows it.
	 */
	public void handleLocalDeleteEvent(){
		acquireLockOnThis();
		logger.trace("handleLocalDeleteEvent - File: {}", getFile().getPath());
		if(isExecuting){
			logger.trace("Event occured for {} while executing.", getFile().getPath());
			nextState = nextState.changeStateOnLocalDelete();
			checkIfChanged();

		} else {
			updateTimestamp();
			currentState = currentState.handleLocalDelete();
			nextState = currentState.getDefaultState();

		}
		releaseLockOnThis();
	}

	public void handleLocalHardDeleteEvent(){
		if(getFile().isFolder()){
			logger.trace("File {}: is a folder", getFile().getPath());
			FolderComposite folder = (FolderComposite)getFile();
			Map<Path, FileComponent> children = folder.getChildren();
			for(Map.Entry<Path, FileComponent> childEntry : children.entrySet()){
				FileComponent child = childEntry.getValue();
				logger.trace("Child {}: handleLocalHardDelete", getFile().getPath());
				child.getAction().handleLocalHardDeleteEvent();
			}
		}
		acquireLockOnThis();
		logger.trace("handleLocalHardDeleteEvent - File: {}", getFile().getPath());
		if(isExecuting){
			logger.trace("Event occured for {} while executing.", getFile().getPath());
			nextState = nextState.changeStateOnLocalHardDelete();
			checkIfChanged();

		} else {
			updateTimestamp();
			currentState = currentState.handleLocalHardDelete();
			nextState = currentState.getDefaultState();
		}
		releaseLockOnThis();

		if(!Files.exists(getFile().getPath())){
			return;
		}
		try {
			Files.delete(getFile().getPath());
			logger.trace("DELETED FROM DISK: {}", getFile().getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void handleLocalMoveEvent(Path oldFilePath) {
		logger.debug("handleLocalMoveEvent - File: {}", getFile().getPath());
		acquireLockOnThis();
		if(isExecuting){
			logger.trace("Event occured for {} while executing.", getFile().getPath());
			nextState = nextState.changeStateOnLocalMove(oldFilePath);
			checkIfChanged();
		} else {
			updateTimestamp();
			if(oldFilePath.equals(getFile().getPath())){
				logger.trace("File {}:Move to same location due to update!", getFile().getPath());
				eventManager.getFileTree().getDeletedByContentHash().get(getFile().getContentHash()).remove(oldFilePath);
				return;
			}
			currentState = currentState.handleLocalMove(oldFilePath);
			nextState = currentState.getDefaultState();

		}
		releaseLockOnThis();
	}

	public void handleRemoteUpdateEvent() {
		logger.trace("handleRemoteUpdateEvent - File: {}", getFile().getPath());
		acquireLockOnThis();
		if(isExecuting){

			logger.trace("Event occured for {} while executing.", getFile().getPath());
//			changedWhileExecuted = true;
			nextState = nextState.changeStateOnRemoteUpdate();
			checkIfChanged();

		} else {
			updateTimestamp();
			currentState = currentState.handleRemoteUpdate();
			nextState = currentState.getDefaultState();
		}
		releaseLockOnThis();
	}

	public void handleRemoteDeleteEvent() {
		logger.trace("handleRemoteDeleteEvent - File: {}", getFile().getPath());

		if(getFile().isFolder()){
			logger.trace("File {}: is a folder", getFile().getPath());
			FolderComposite folder = (FolderComposite)getFile();
			Map<Path, FileComponent> children = folder.getChildren();

//			Vector<FileComponent> children = new Vector<FileComponent>(folder.getChildren().values());
			for(Map.Entry<Path, FileComponent> childEntry : children.entrySet()){
				FileComponent child = childEntry.getValue();
				logger.trace("Child {}: handleLocalHardDelete", getFile().getPath());
				child.getAction().handleRemoteDeleteEvent();
			}
		}

		acquireLockOnThis();
		if(isExecuting){

			logger.trace("Event occured for {} while executing.", getFile().getPath());
			nextState = nextState.changeStateOnRemoteDelete();
			checkIfChanged();

		} else {
			updateTimestamp();
			currentState = currentState.handleRemoteDelete();
			nextState = currentState.getDefaultState();
		}
		releaseLockOnThis();
	}

	public void handleRemoteCreateEvent() {
		logger.trace("handleRemoteCreateEvent - File: {}", getFile().getPath());
		acquireLockOnThis();
		if(isExecuting){

			logger.trace("Event occured for {} while executing.", getFile().getPath());

			nextState = nextState.changeStateOnRemoteCreate();
			checkIfChanged();

		} else {
			updateTimestamp();
			currentState = currentState.handleRemoteCreate();
			nextState = currentState.getDefaultState();
		}
		releaseLockOnThis();
	}

	private void checkIfChanged() {
		if(!(nextState instanceof EstablishedState)){
			logger.trace("File {}: Next state is of type {}, keep track of change", getFile().getPath(), nextState.getClass());
			changedWhileExecuted = true;
		} else {
			logger.trace("File {}: Next state is of type {}, no change detected", getFile().getPath(), nextState.getClass());
		}
	}

	public void handleRemoteMoveEvent(Path path) {
		logger.trace("handleRemoteMoveEvent - File: {}", getFile().getPath());
		Path oldPath = getFile().getPath();
		acquireLockOnThis();
		if(isExecuting){

			logger.trace("Event occured for {} while executing.", getFile().getPath());
//			changedWhileExecuted = true;
			nextState = nextState.changeStateOnRemoteMove(path);
			checkIfChanged();

		} else {
			logger.trace("Currentstate: {} {}", getFile().getPath(), getCurrentState().getClass());
			updateTimestamp();
			currentState = currentState.handleRemoteMove(path);
			nextState = currentState.getDefaultState();

			if(!Files.exists(oldPath)){
				return;
			}
			try {
				com.google.common.io.Files.move(oldPath.toFile(), path.toFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		releaseLockOnThis();
	}


	/**
	 * Each state is able to execute an action as soon the state is considered as stable.
	 * The action itself depends on the current state (e.g. add file, delete file, etc.)
	 * @return
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 * @throws IllegalFileLocation
	 * @throws InvalidProcessStateException
	 */
	public ExecutionHandle execute(IFileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException {
		// this may be async, i.e. do not wait on completion of the process
		// maybe return the IProcessComponent object such that the
		// executor can be aware of the status (completion of task etc)
		ExecutionHandle ehandle = null;
		try{
			setIsExecuting(true);
			executionAttempts++;
			ehandle = currentState.execute(fileManager);
		} catch (Throwable t){
			// FIXME: Why catch throwable? Why here? Would this block an execution slot?
			logger.error("onLocalFileModified: Catched a throwable of type {} with message {}", t.getClass().toString(),  t.getMessage());
			for(int i = 0; i < t.getStackTrace().length; i++){
				StackTraceElement curr = t.getStackTrace()[i];
				logger.error("{} : {} ", curr.getClassName(), curr.getMethodName());
				logger.error("{} : {} ", curr.getFileName(), curr.getLineNumber());
			}
		}

		return ehandle;
	}

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return current state object
	 */
	public AbstractActionState getCurrentState() {
		logger.trace("Current state of {} is {}", getFile().getPath(), currentState.getClass());
		return currentState;
	}

	public synchronized void addEventListener(IActionEventListener listener) {
		eventListeners.add(listener);
	}

	public Set<IActionEventListener> getEventListener() {
		return eventListeners;
	}

	public int getExecutionAttempts() {
		return executionAttempts;
	}

//	public void putFile(String string, FileComponent file) {
//		eventManager.getFileTree().putComponent(string, file);
//	}

	@Override
	public void onSucceed() {
		setIsExecuting(false);
		logger.trace("onSucceed: File {}. Switch state from {} to {}", getFile().getPath(), currentState.getClass(), nextState.getClass());
		currentState.performCleanup();
		currentState = nextState;
		nextState = nextState.getDefaultState();
		changedWhileExecuted = false;
	}

	@Override
	public AbstractActionState getNextState() {
		// TODO Auto-generated method stub
		return nextState;
	}

	@Override
	public boolean isExecuting() {
		return isExecuting;
	}

	private void setIsExecuting(boolean isExecuting){
		this.isExecuting = isExecuting;
	}

	@Override
	public boolean getChangedWhileExecuted() {
		// TODO Auto-generated method stub
		return changedWhileExecuted;
	}

	@Override
	public void onFailed() {
		setIsExecuting(false);
		isExecuting = false;
	}

	@Override
	public Lock getLock() {
		return lock;
	}

}

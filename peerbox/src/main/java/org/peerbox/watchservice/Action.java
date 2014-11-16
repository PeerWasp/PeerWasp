package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.states.AbstractActionState;
import org.peerbox.watchservice.states.LocalCreateState;
import org.peerbox.watchservice.states.LocalDeleteState;
import org.peerbox.watchservice.states.InitialState;
import org.peerbox.watchservice.states.LocalUpdateState;
import org.peerbox.watchservice.states.LocalMoveState;
import org.peerbox.watchservice.states.ConflictState;
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

//public class Action implements IProcessExceptionElement{
public class Action{
	private final static Logger logger = LoggerFactory.getLogger(Action.class);
	private long timestamp = Long.MAX_VALUE;
	
	private Path filePath;
	private AbstractActionState currentState;
	private Set<IActionEventListener> eventListeners;
	private int executionAttempts = 0;
	
	private boolean isUploaded = false;
	
	/**
	 * Initialize with timestamp and set currentState to initial state
	 */
	public Action(Path filePath){
		this.filePath = filePath;
		currentState = new InitialState(this);
		eventListeners = new HashSet<IActionEventListener>();
		updateTimestamp();
	}
	
	public void updateTimestamp() {
		timestamp = System.currentTimeMillis();
	}
	
	public static String createStringFromByteArray(byte[] bytes){
		String hashString = Base64.getEncoder().encodeToString(bytes);
		return hashString;
	}
	
	public boolean getIsUploaded(){
		return isUploaded;
	}
	
	public void setIsUploaded(boolean isUploaded){
		this.isUploaded = isUploaded;
	}
	
	/**
	 * changes the state of the currentState to Create state if current state allows it.
	 */
	public void handleLocalCreateEvent(){
		currentState = currentState.handleLocalCreateEvent();
		updateTimestamp();
	}
	
	/**
	 * changes the state of the currentState to Modify state if current state allows it.
	 */
	public void handleLocalModifyEvent(){
		currentState = currentState.handleLocalUpdateEvent();
		updateTimestamp();
	}

	/**
	 * changes the state of the currentState to Delete state if current state allows it.
	 */
	public void handleLocalDeleteEvent(){
		currentState = currentState.handleLocalDeleteEvent();
		updateTimestamp();
	}
	
	public void handleLocalMoveEvent(Path oldFilePath) {
		currentState = currentState.handleLocalMoveEvent(oldFilePath);
		updateTimestamp();
	}
	
	public void handleRemoteUpdateEvent() {
		currentState = currentState.handleRemoteUpdateEvent();
		updateTimestamp();
	}
	
	public void handleRemoteDeleteEvent() {
		currentState = currentState.handleRemoteDeleteEvent();
		updateTimestamp();
	}
	
	public void handleRecoverEvent(int versionToRecover){
		currentState = currentState.handleRecoverEvent(versionToRecover);
		updateTimestamp();
	}

	/**
	 * Each state is able to execute an action as soon the state is considered as stable. 
	 * The action itself depends on the current state (e.g. add file, delete file, etc.)
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 * @throws IllegalFileLocation
	 * @throws InvalidProcessStateException 
	 */
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation, InvalidProcessStateException {
		// this may be async, i.e. do not wait on completion of the process
		// maybe return the IProcessComponent object such that the
		// executor can be aware of the status (completion of task etc)
		executionAttempts++;
		currentState.execute(fileManager);
		currentState = currentState.getDefaultState();
		//currentState = new InitialState(this);
	}
	
	public Path getFilePath(){
		return filePath;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @return current state object
	 */
	public AbstractActionState getCurrentState() {
		if (currentState.getClass() == LocalCreateState.class) {
			logger.debug("Current State: Create");
		} else if (currentState.getClass() == LocalDeleteState.class) {
			logger.debug("Current State: Delete");
		} else if (currentState.getClass() == LocalUpdateState.class) {
			logger.debug("Current State: Modify");
		} else if (currentState.getClass() == LocalMoveState.class) {
			logger.debug("Current State: Move");
		} else if (currentState.getClass() == InitialState.class) {
			logger.debug("Current State: Initial");
		} else if (currentState.getClass() == ConflictState.class) {
			logger.debug("Current State: Conflict");
		}

		return currentState;
	}

	public void setPath(Path path) {
		this.filePath = path;
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

//	@Override
//	public void handleException(ProcessExceptionVisitor visitor) {
//		// TODO Auto-generated method stub
//		visitor.visit(this);
//		System.out.println("Action handle exception");
//	}
}

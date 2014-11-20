package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;
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

public class Action implements IAction{
	private final static Logger logger = LoggerFactory.getLogger(Action.class);
	private long timestamp = Long.MAX_VALUE;
	
	private AbstractActionState currentState;
	private Set<IActionEventListener> eventListeners;
	private int executionAttempts = 0;
	private IFileEventManager eventManager;
	private FileComponent file;
	
	private boolean isUploaded = false;
	
	/**
	 * Initialize with timestamp and set currentState to initial state
	 */
	public Action(IFileEventManager fileEventManager){
		currentState = new InitialState(this);
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
//		logger.debug("Path: {} State: {} HashCode: {}", filePath, currentState.getClass(), this.hashCode());
		updateTimestamp();
		currentState = currentState.handleLocalCreate();

	}
	
	/**
	 * changes the state of the currentState to Modify state if current state allows it.
	 */
	public void handleLocalUpdateEvent(){
//		logger.debug("Path: {} State: {} HashCode: {}", filePath, currentState.getClass(), this.hashCode());
		updateTimestamp();
		currentState = currentState.handleLocalUpdate();
	}

	/**
	 * changes the state of the currentState to Delete state if current state allows it.
	 */
	public void handleLocalDeleteEvent(){
//		logger.debug("Path: {} State: {} HashCode: {}", filePath, currentState.getClass(), this.hashCode());
		updateTimestamp();
		currentState = currentState.handleLocalDelete();
	}
	
	public void handleLocalMoveEvent(Path oldFilePath) {
//		logger.debug("Path: {} State: {} HashCode: {}", filePath, currentState.getClass(), this.hashCode());
		updateTimestamp();
		currentState = currentState.handleLocalMove(oldFilePath);

	}
	
	public void handleRemoteUpdateEvent() {
//		logger.debug("Path: {} State: {} HashCode: {}", filePath, currentState.getClass(), this.hashCode());
		updateTimestamp();
		currentState = currentState.handleRemoteUpdate();
		
	}
	
	public void handleRemoteDeleteEvent() {
//		logger.debug("Path: {} State: {} HashCode: {}", filePath, currentState.getClass(), this.hashCode());
//		currentState = currentState.changeStateOnRemoteDelete();
		updateTimestamp();
		currentState = currentState.handleRemoteDelete();
	}
	
	public void handleRecoverEvent(int versionToRecover){
//		logger.debug("Path: {} State: {} HashCode: {}", filePath, currentState.getClass(), this.hashCode());
		currentState = currentState.handleLocalRecover(versionToRecover);
		updateTimestamp();
	}
	
	public void handleRemoteCreateEvent() {
//		logger.debug("Path: {} State: {} HashCode: {}", filePath, currentState.getClass(), this.hashCode());
		currentState = currentState.handleRemoteCreate();
		updateTimestamp();
	}
	
	public void handleRemoteMoveEvent(Path path) {
//		logger.debug("Path: {} State: {} HashCode: {}", filePath, currentState.getClass(), this.hashCode());
		updateTimestamp();
		currentState = currentState.handleRemoteMove(path);
		
//		try {
//			logger.debug("Move From {} to {}", path, getFilePath());
//			com.google.common.io.Files.move(getFilePath().toFile(), path.toFile());
//			Files.move(path, getFilePath() );
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
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
		try{
		
		executionAttempts++;
		currentState.execute(fileManager);
//		currentState = currentState.getDefaultState();
		//currentState = new InitialState(this);
		
		} catch (Throwable t){
			logger.error("onLocalFileModified: Catched a throwable of type {} with message {}", t.getClass().toString(),  t.getMessage());
			for(int i = 0; i < t.getStackTrace().length; i++){
				StackTraceElement curr = t.getStackTrace()[i];
				logger.error("{} : {} ", curr.getClassName(), curr.getMethodName());
				logger.error("{} : {} ", curr.getFileName(), curr.getLineNumber());
			}
		}
	}
	
	public Path getFilePath(){
		return file.getPath();
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @return current state object
	 */
	public AbstractActionState getCurrentState() { 
		logger.debug("Current path: {}", getFilePath());
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
		} else {
			logger.debug("Current State: {}", currentState.getClass().getName());
		}

		return currentState;
	}

//	public void setPath(Path path) {
//		this.filePath = path;
//	}
	
	public synchronized void addEventListener(IActionEventListener listener) {
		eventListeners.add(listener);
	}
	
	public Set<IActionEventListener> getEventListener() {
		return eventListeners;
	}

	public int getExecutionAttempts() {
		return executionAttempts;
	}

	public void putFile(String string, FileComponent file) {
		// TODO Auto-generated method stub
		eventManager.getFileTree().putComponent(string, file);
	}

	@Override
	public void onSucceed() {
		System.out.println("CurrentState: " + currentState.getClass());
		currentState = currentState.getDefaultState();
		System.out.println("New CurrentState: " + currentState.getClass());
	}

//	@Override
//	public void handleException(ProcessExceptionVisitor visitor) {
//		// TODO Auto-generated method stub
//		visitor.visit(this);
//		System.out.println("Action handle exception");
//	}
}

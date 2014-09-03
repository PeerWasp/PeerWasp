package org.peerbox.watchservice;

import java.io.File;
import java.util.Calendar;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
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

public class Action {
	
	private final static Logger logger = LoggerFactory.getLogger(Action.class);
	private long timestamp = Long.MAX_VALUE;
	
	//TODO File path?
	private File file;
	
	private ActionState currentState;
	
	/**
	 * Initialize with timestamp and set currentState to initial state
	 */
	public Action(){
		timestamp = Calendar.getInstance().getTimeInMillis(); 
		currentState = new InitialState();
	}
	
	public Action(ActionState initialState){
		currentState = initialState;
		timestamp = Calendar.getInstance().getTimeInMillis();
	}
	
	public void setTimeStamp(long timestamp) {
		// TODO Auto-generated method stub
		if(timestamp < this.timestamp){
			//this is clearly an error - but can it even occur?
		}
		this.timestamp = timestamp;
	}
	
	/**
	 * changes the state of the currentState to Create state if current state allows it.
	 */
	public void handleCreateEvent(){
		currentState = currentState.handleCreateEvent();
	}
	
	/**
	 * changes the state of the currentState to Delete state if current state allows it.
	 */
	public void handleDeleteEvent(){
		currentState = currentState.handleDeleteEvent();
	}
	
	/**
	 * changes the state of the currentState to Modify state if current state allows it.
	 */
	public void handleModifyEvent(){
		currentState = currentState.handleModifyEvent();
	}
	
	/**
	 * Each state is able to execute an action as soon the state is considered as stable. 
	 * The action itself depends on the current state (e.g. add file, delete file, etc.)
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 * @throws IllegalFileLocation
	 */
	//execute action depending on state
	public void execute() throws NoSessionException, NoPeerConnectionException, IllegalFileLocation{
		logger.debug("Execute action...");
		// this may be async, i.e. do not wait on completion of the process
		// maybe return the IProcessComponent object such that the 
		// executor can be aware of the status (completion of task etc)

		currentState.execute(file);
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @return current state object
	 */
	public ActionState getCurrentState(){	
			if (currentState.getClass() == CreateState.class){
				logger.debug("Current State: Create");
			} else if (currentState.getClass() == DeleteState.class){
				logger.debug("Current State: Delete");
			} else if (currentState.getClass() == ModifyState.class){
				logger.debug("Current State: Modify");
			} else if (currentState.getClass() == MoveState.class){
				logger.debug("Current State: Move");
			} else if (currentState.getClass() == InitialState.class){
				logger.debug("Current State: Initial");
			}
			
			return currentState;
		}
}

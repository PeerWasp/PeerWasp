package org.peerbox.watchservice;

import java.io.File;
import java.util.Calendar;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Action {
	
	private final static Logger logger = LoggerFactory.getLogger(Action.class);
	private long timestamp = Long.MAX_VALUE;
	
	//TODO File path?
	private File file;
	
	private ActionState currentState;
		
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
	
	
	public void handleCreateEvent(){
		currentState = currentState.handleCreateEvent();
	}
	
	public void handleDeleteEvent(){
		currentState = currentState.handleDeleteEvent();
	}
	
	public void handleModifyEvent(){
		currentState = currentState.handleModifyEvent();
	}
	
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
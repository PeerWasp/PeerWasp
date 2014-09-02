package org.peerbox.watchservice;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Action {
	
	private final static Logger logger = LoggerFactory.getLogger(Action.class);
	private long timestamp = Long.MAX_VALUE;
	
	private ActionState currentState;
		
	public Action(){
		timestamp = Calendar.getInstance().getTimeInMillis();
		ActionState initialState = new InitialState();
		currentState = initialState;
		

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
	
	
	public void createEvent(){
		currentState = currentState.handleCreateEvent();
	}
	
	public void deleteEvent(){
		currentState = currentState.handleDeleteEvent();
	}
	
	public void modifyEvent(){
		currentState = currentState.handleModifyEvent();
	}
	
	//execute action depending on state
	public void execute(){
		logger.debug("Execute action...");
		// this may be async, i.e. do not wait on completion of the process
		// maybe return the IProcessComponent object such that the 
		// executor can be aware of the status (completion of task etc)

		currentState.execute();
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public ActionState getCurrentState(){	
		if (currentState.getClass() == CreateState.class){
			System.out.println("Current State: Create");
		} else if (currentState.getClass() == DeleteState.class){
			System.out.println("Current State: Delete");
		} else if (currentState.getClass() == ModifyState.class){
			System.out.println("Current State: Modify");
		} else if (currentState.getClass() == MoveState.class){
			System.out.println("Current State: Move");
		} else if (currentState.getClass() == InitialState.class){
			System.out.println("Current State: Initial");
		}
		
		return currentState;
	}
}

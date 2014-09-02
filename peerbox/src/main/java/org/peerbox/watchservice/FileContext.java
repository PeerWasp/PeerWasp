package org.peerbox.watchservice;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileContext {
	
	private final static Logger logger = LoggerFactory.getLogger(FileAction.class);
	private long timestamp = Long.MAX_VALUE;
	
	private FileActionState currentState;
		
	public FileContext(){
		timestamp = Calendar.getInstance().getTimeInMillis();
		FileActionState initialState = new StartActionState();
		currentState = initialState;
		

	}
	
	public FileContext(FileActionState initialState){
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
	
	public FileActionState getCurrentState(){	
		if (currentState.getClass() == CreateFileAction.class){
			System.out.println("Current State: Create");
		} else if (currentState.getClass() == DeleteFileAction.class){
			System.out.println("Current State: Delete");
		} else if (currentState.getClass() == ModifyFileAction.class){
			System.out.println("Current State: Modify");
		} else if (currentState.getClass() == MoveFileAction.class){
			System.out.println("Current State: Move");
		} else if (currentState.getClass() == StartActionState.class){
			System.out.println("Current State: Initial");
		}
		
		return currentState;
	}
}

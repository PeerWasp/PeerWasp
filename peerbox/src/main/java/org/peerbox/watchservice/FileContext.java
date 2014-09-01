package org.peerbox.watchservice;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileContext {
	
	private final static Logger logger = LoggerFactory.getLogger(FileAction.class);
	private long timestamp = Long.MAX_VALUE;
	
	private FileActionState currentState;
	
	private FileActionState createState;
	private FileActionState deleteState;
	private FileActionState modifyState;
	private FileActionState initialState;
		
	public FileContext(){
		timestamp = Calendar.getInstance().getTimeInMillis();

		createState = new CreateFileAction(this);
		deleteState = new DeleteFileAction(this);
		modifyState = new ModifyFileAction(this);
		initialState = new StartActionState(this);
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
	
	//Set current state
	public void setState(FileActionState state){
		this.currentState = state;
	}
	
	//getter for all possible states
	public FileActionState getCreateState(){
		return createState;
	}
	
	public FileActionState getDeleteState(){
		return deleteState;
	}
	
	public FileActionState getModifyState(){
		return modifyState;
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
		if (currentState == createState){
			System.out.println("current state: create");
		} else if (currentState == deleteState){
			System.out.println("current state: delete");
		} else if (currentState == modifyState){
			System.out.println("current state: modify");
		} else {
			System.out.println("current state: initial");
		}
		
		return currentState;
	}
}

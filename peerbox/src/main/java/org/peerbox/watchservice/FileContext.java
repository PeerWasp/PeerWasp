package org.peerbox.watchservice;

public class FileContext {

	private FileActionState currentState;
	
	private FileActionState createState;
	private FileActionState deleteState;
	private FileActionState modifyState;
		
	public FileContext(){
		createState = new CreateFileAction(this);
		deleteState = new DeleteFileAction(this);
		modifyState = new ModifyFileAction(this);
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
		currentState.execute();
	}
	
}

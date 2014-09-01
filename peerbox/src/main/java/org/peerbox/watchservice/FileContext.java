package org.peerbox.watchservice;

public class FileContext {

	private FileActionState state;
	
	public FileContext(){
		state = null;
	}
	
	public void setSate(FileActionState state){
		this.state = state;
	}
	
	public FileActionState getState(){
		return state;
	}
	
}

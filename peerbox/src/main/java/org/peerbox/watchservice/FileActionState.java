package org.peerbox.watchservice;

public interface FileActionState {
	
	public void changeToDeleteState(FileContext context);
	
	public void changeToCreateState(FileContext context);
	
	public void changeToModifyState(FileContext context);
	
	public void changeToMoveState(FileContext context);
	
	public void execute();
}
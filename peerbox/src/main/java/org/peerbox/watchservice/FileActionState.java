package org.peerbox.watchservice;

public interface FileActionState {
	
	public void handleCreateEvent();
	public void handleDeleteEvent();
	public void handleModifyEvent();
	public void execute();
}
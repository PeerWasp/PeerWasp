package org.peerbox.watchservice;

public interface FileActionState {
	
	public FileActionState handleCreateEvent();
	public FileActionState handleDeleteEvent();
	public FileActionState handleModifyEvent();
	public void execute();
}
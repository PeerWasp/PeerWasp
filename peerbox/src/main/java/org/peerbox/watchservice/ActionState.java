package org.peerbox.watchservice;

public interface ActionState {
	
	public ActionState handleCreateEvent();
	public ActionState handleDeleteEvent();
	public ActionState handleModifyEvent();
	public void execute();
}
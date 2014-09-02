package org.peerbox.watchservice;

public class MoveState implements ActionState {
	
	@Override
	public ActionState handleCreateEvent() {
		System.out.println("Create Request denied: Cannot change from Move to Create.");
		return new MoveState();
	}

	@Override
	public ActionState handleDeleteEvent() {
		System.out.println("Delete Request denied: Cannot change from Move to Delete.");
		return new MoveState();
	}

	@Override
	public ActionState handleModifyEvent() {
		System.out.println("Modify Request denied: Cannot change from Move to Modify State.");
		return new MoveState();
	}
	
	@Override
	public void execute() {
		System.out.println("Delete State: Execute H2H API call");
		
	}


}

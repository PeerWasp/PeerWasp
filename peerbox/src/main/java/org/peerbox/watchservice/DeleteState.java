package org.peerbox.watchservice;

public class DeleteState implements ActionState {
	
	@Override
	public ActionState handleCreateEvent() {
		System.out.println("Create Request accepted: Move detected.");
		return new MoveState();
	}

	@Override
	public ActionState handleDeleteEvent() {
		System.out.println("Delete Request denied: Already in Delete State.");
		return new DeleteState();
	}

	@Override
	public ActionState handleModifyEvent() {
		System.out.println("Modify Request denied: Cannot change from Delete to Modify State.");
		return new DeleteState();
	}
	
	
	@Override
	public void execute() {
		System.out.println("Delete State: Execute H2H API call");
		
	}


}

package org.peerbox.watchservice;

public class MoveFileAction implements FileActionState {
	
	@Override
	public FileActionState handleCreateEvent() {
		System.out.println("Create Request denied: Cannot change from Move to Create.");
		return new MoveFileAction();
	}

	@Override
	public FileActionState handleDeleteEvent() {
		System.out.println("Delete Request denied: Cannot change from Move to Delete.");
		return new MoveFileAction();
	}

	@Override
	public FileActionState handleModifyEvent() {
		System.out.println("Modify Request denied: Cannot change from Move to Modify State.");
		return new MoveFileAction();
	}
	
	@Override
	public void execute() {
		System.out.println("Delete State: Execute H2H API call");
		
	}


}

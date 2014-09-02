package org.peerbox.watchservice;

public class DeleteFileAction implements FileActionState {
	
	@Override
	public FileActionState handleCreateEvent() {
		System.out.println("Create Request accepted: Move detected.");
		return new MoveFileAction();
	}

	@Override
	public FileActionState handleDeleteEvent() {
		System.out.println("Delete Request denied: Already in Delete State.");
		return new DeleteFileAction();
	}

	@Override
	public FileActionState handleModifyEvent() {
		System.out.println("Modify Request denied: Cannot change from Delete to Modify State.");
		return new DeleteFileAction();
	}
	
	
	@Override
	public void execute() {
		System.out.println("Delete State: Execute H2H API call");
		
	}


}

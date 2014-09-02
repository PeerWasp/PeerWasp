package org.peerbox.watchservice;

public class StartActionState implements FileActionState {

	@Override
	public FileActionState handleCreateEvent() {
		System.out.println("Create Request accepted: State changed from Initial to Create.");
		return new CreateFileAction();
	}

	@Override
	public FileActionState handleDeleteEvent() {
		System.out.println("Delete Request accepted: State changed from Initial to Delete.");
		return new DeleteFileAction();
	}

	@Override
	public FileActionState handleModifyEvent() {
		System.out.println("Modify Request accepted: State changed from Initial to Modify.");
		return new ModifyFileAction();
		
	}
	
	@Override
	public void execute() {
		System.out.println("Initial State: API call not possible.");
		
	}
}

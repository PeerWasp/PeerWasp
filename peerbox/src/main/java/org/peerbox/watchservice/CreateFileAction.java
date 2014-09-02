package org.peerbox.watchservice;

public class CreateFileAction implements FileActionState {

	//State must be known in order to set the new state

	public CreateFileAction(){

	}
	
	@Override
	public FileActionState handleCreateEvent() {
		System.out.println("Create Request denied: Already in Create State.");
		return new CreateFileAction();
	}

	@Override
	public FileActionState handleDeleteEvent() {
		System.out.println("Delete Request accepted: State changed from Create to Delete.");
		return new DeleteFileAction();
	}

	@Override
	public FileActionState handleModifyEvent() {
		System.out.println("Modify Request accepted: State changed from Create to Modify.");
		return new ModifyFileAction();
		
	}
	
	@Override
	public void execute() {
		System.out.println("Create State: Execute H2H API call");
		
	}
}

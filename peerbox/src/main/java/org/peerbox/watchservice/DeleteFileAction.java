package org.peerbox.watchservice;

public class DeleteFileAction implements FileActionState {

	//State must be known in order to set the new state
	private final FileContext _context;
	
	public DeleteFileAction(FileContext context){
		_context = context;
	}
	
	@Override
	public void handleCreateEvent() {
		System.out.println("Create Request accepted: Move detected.");
	}

	@Override
	public void handleDeleteEvent() {
		System.out.println("Delete Request denied: Already in Delete State.");
	}

	@Override
	public void handleModifyEvent() {
		System.out.println("Modify Request denied: Cannot change from Delete to Modify State.");
		
	}
	
	@Override
	public void execute() {
		System.out.println("Delete State: Execute H2H API call");
		
	}
}

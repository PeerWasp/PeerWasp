package org.peerbox.watchservice;

public class CreateFileAction implements FileActionState {

	//State must be known in order to set the new state
	private final FileContext _context;
	
	public CreateFileAction(FileContext context){
		_context = context;
	}
	
	@Override
	public void handleCreateEvent() {
		System.out.println("Create Request denied: Already in Create State.");
	}

	@Override
	public void handleDeleteEvent() {
		_context.setState(_context.getDeleteState());
		System.out.println("Delete Request accepted: State changed from Create to Delete.");
	}

	@Override
	public void handleModifyEvent() {
		_context.setState(_context.getModifyState());
		System.out.println("Modify Request accepted: State changed from Create to Modify.");
		
	}
	
	@Override
	public void execute() {
		System.out.println("Create State: Execute H2H API call");
		
	}
}

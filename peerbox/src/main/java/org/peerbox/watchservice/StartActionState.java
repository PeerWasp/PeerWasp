package org.peerbox.watchservice;

public class StartActionState implements FileActionState {

	//State must be known in order to set the new state
	private final FileContext _context;
	
	public StartActionState(){
		_context = null;
	}
	
	@Override
	public void handleCreateEvent() {
		_context.setState(_context.getCreateState());
		System.out.println("Create Request accepted: State changed from Initial to Create.");
	}

	@Override
	public void handleDeleteEvent() {
		_context.setState(_context.getDeleteState());
		System.out.println("Delete Request accepted: State changed from Initial to Delete.");
	}

	@Override
	public void handleModifyEvent() {
		_context.setState(_context.getModifyState());
		System.out.println("Modify Request accepted: State changed from Initial to Modify.");
		
	}
	
	@Override
	public void execute() {
		System.out.println("Initial State: API call not possible.");
		
	}
}

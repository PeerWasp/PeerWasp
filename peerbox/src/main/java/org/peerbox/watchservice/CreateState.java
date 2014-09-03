package org.peerbox.watchservice;

public class CreateState implements ActionState {

	//State must be known in order to set the new state

	public CreateState(){

	}
	
	@Override
	public ActionState handleCreateEvent() {
		System.out.println("Create Request denied: Already in Create State.");
		return new CreateState();
	}

	@Override
	public ActionState handleDeleteEvent() {
		System.out.println("Delete Request accepted: State changed from Create to Initial State.");
		return new InitialState();
	}

	@Override
	public ActionState handleModifyEvent() {
		System.out.println("Modify Request accepted: State changed from Create to Modify.");
		return new CreateState();
		
	}
	
	@Override
	public void execute() {
		System.out.println("Create State: Execute H2H API call");
		
	}
}

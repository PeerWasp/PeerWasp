package org.peerbox.watchservice;

public class InitialState implements ActionState {

	@Override
	public ActionState handleCreateEvent() {
		System.out.println("Create Request accepted: State changed from Initial to Create.");
		return new CreateState();
	}

	@Override
	public ActionState handleDeleteEvent() {
		System.out.println("Delete Request accepted: State changed from Initial to Delete.");
		return new DeleteState();
	}

	@Override
	public ActionState handleModifyEvent() {
		System.out.println("Modify Request accepted: State changed from Initial to Modify.");
		return new ModifyState();
		
	}
	
	@Override
	public void execute() {
		System.out.println("Initial State: API call not possible.");
		
	}
}

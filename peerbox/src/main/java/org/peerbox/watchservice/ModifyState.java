package org.peerbox.watchservice;

public class ModifyState implements ActionState {
		
		@Override
		public ActionState handleCreateEvent() {
			System.out.println("Create Request denied: Cannot change from Modify to Create State.");
			return new ModifyState();
		}

		@Override
		public ActionState handleDeleteEvent() {
			System.out.println("Delete Request accepted: State changed from Modify to Delete.");
			return new DeleteState();
		}

		@Override
		public ActionState handleModifyEvent() {
			System.out.println("Modify Request denied: Already in Modify State.");
			return new ModifyState();
			
		}
		
		@Override
		public void execute() {
			System.out.println("Create State: Execute H2H API call");
			
		}

}

package org.peerbox.watchservice;

public class ModifyFileAction implements FileActionState {
		
		@Override
		public FileActionState handleCreateEvent() {
			System.out.println("Create Request denied: Cannot change from Modify to Create State.");
			return new ModifyFileAction();
		}

		@Override
		public FileActionState handleDeleteEvent() {
			System.out.println("Delete Request accepted: State changed from Modify to Delete.");
			return new DeleteFileAction();
		}

		@Override
		public FileActionState handleModifyEvent() {
			System.out.println("Modify Request denied: Already in Modify State.");
			return new ModifyFileAction();
			
		}
		
		@Override
		public void execute() {
			System.out.println("Create State: Execute H2H API call");
			
		}

}

package org.peerbox.watchservice;

public class ModifyFileAction implements FileActionState {

	//State must be known in order to set the new state
		private final FileContext _context;
		
		public ModifyFileAction(FileContext context){
			_context = context;
		}
		
		@Override
		public void handleCreateEvent() {
			System.out.println("Create Request denied: Cannot change from Modify to Create State.");
		}

		@Override
		public void handleDeleteEvent() {
			_context.setState(_context.getDeleteState());
			System.out.println("Delete Request accepted: State changed from Modify to Delete.");
		}

		@Override
		public void handleModifyEvent() {
			System.out.println("Modify Request denied: Already in Modify State.");
			
		}
		
		@Override
		public void execute() {
			System.out.println("Create State: Execute H2H API call");
			
		}

}

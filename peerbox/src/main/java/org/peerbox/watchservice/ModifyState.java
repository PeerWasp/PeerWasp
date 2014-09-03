package org.peerbox.watchservice;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.model.H2HManager;

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
		public void execute(File file) throws NoSessionException, IllegalArgumentException, NoPeerConnectionException {
			System.out.println("Modify State: Execute H2H \"Modify File\" API call");
			H2HManager manager = new H2HManager();
			IFileManager fileHandler = manager.getNode().getFileManager();
			
			fileHandler.update(file);
			System.out.println("Task \"Update File\" executed.");
		}

}

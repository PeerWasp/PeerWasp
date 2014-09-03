package org.peerbox.watchservice;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.model.H2HManager;

public class MoveState implements ActionState {
	
	@Override
	public ActionState handleCreateEvent() {
		System.out.println("Create Request denied: Cannot change from Move to Create.");
		return new MoveState();
	}

	@Override
	public ActionState handleDeleteEvent() {
		System.out.println("Delete Request accepted: State changed from Move to Delete.");
		return new DeleteState();
	}

	@Override
	public ActionState handleModifyEvent() {
		System.out.println("Modify Request denied: Cannot change from Move to Modify State.");
		return new MoveState();
	}
	
	@Override
	public void execute(File file) throws NoSessionException, NoPeerConnectionException {
		System.out.println("Move State: Execute \"Move File\" H2H API call");
		H2HManager manager = new H2HManager();
		IFileManager fileHandler = manager.getNode().getFileManager();
		
		//H2H move needs to be analyzed to make sure how it works
		fileHandler.move(file,file);
		System.out.println("Task \"Add File\" executed.");
		
	}


}

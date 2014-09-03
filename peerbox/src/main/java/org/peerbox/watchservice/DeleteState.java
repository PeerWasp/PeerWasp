package org.peerbox.watchservice;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.model.H2HManager;

public class DeleteState implements ActionState {
	
	@Override
	public ActionState handleCreateEvent() {
		System.out.println("Create Request accepted: Move detected.");
		return new MoveState();
	}

	@Override
	public ActionState handleDeleteEvent() {
		System.out.println("Delete Request denied: Already in Delete State.");
		return new DeleteState();
	}

	@Override
	public ActionState handleModifyEvent() {
		System.out.println("Modify Request denied: Cannot change from Delete to Modify State.");
		return new DeleteState();
	}
	
	
	@Override
	public void execute(File file) throws NoSessionException, NoPeerConnectionException {
		System.out.println("Delete State: Execute H2H \"Delete File\" API call");
		H2HManager manager = new H2HManager();
		IFileManager fileHandler = manager.getNode().getFileManager();
		
		fileHandler.delete(file);
		System.out.println("Task \"Delete File\" executed.");
		
	}


}

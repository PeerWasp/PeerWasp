package org.peerbox.watchservice;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.model.H2HManager;

public class CreateState implements ActionState {

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
	public void execute(File file) throws NoSessionException, NoPeerConnectionException, IllegalFileLocation {
		System.out.println("Create State: Execute H2H \"Add File\" API call");
		H2HManager manager = new H2HManager();
		IFileManager fileHandler = manager.getNode().getFileManager();
		
		fileHandler.add(file);
		System.out.println("Task \"Add File\" executed.");
	}
}

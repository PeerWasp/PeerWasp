package org.peerbox.watchservice;

import java.io.File;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;

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
	public void execute(File file) throws NoSessionException, NoPeerConnectionException,
			IllegalFileLocation {
		// TODO Auto-generated method stub
		System.out.println("Execute method in Initial State not defined.");
	}
	
	
}

package org.peerbox.watchservice;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateState implements ActionState {

	private final static Logger logger = LoggerFactory.getLogger(CreateState.class);
	
	public CreateState(){

	}
	
	@Override
	public ActionState handleCreateEvent() {
		logger.debug("Create Request denied: Already in Create State.");
		return new CreateState();
	}

	@Override
	public ActionState handleDeleteEvent() {
		logger.debug("Delete Request accepted: State changed from Create to Delete.");
		return new DeleteState();
	}

	@Override
	public ActionState handleModifyEvent() {
		logger.debug("Modify Request accepted: State changed from Create to Modify.");
		return new ModifyState();
		
	}
	
	@Override
	public void execute(File file) throws NoSessionException, NoPeerConnectionException, IllegalFileLocation {
		logger.debug("Create State: Execute H2H \"Add File\" API call");
		H2HManager manager = new H2HManager();
		IFileManager fileHandler = manager.getNode().getFileManager();
		
		fileHandler.add(file);
		logger.debug("Task \"Add File\" executed.");
	}
}

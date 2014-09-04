package org.peerbox.watchservice;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialState implements ActionState {

	private final static Logger logger = LoggerFactory.getLogger(InitialState.class);
	
	@Override
	public ActionState handleCreateEvent() {
		logger.debug("Create Request accepted: State changed from Initial to Create.");
		return new CreateState();
	}

	@Override
	public ActionState handleDeleteEvent() {
		logger.debug("Delete Request accepted: State changed from Initial to Delete.");
		return new DeleteState();
	}

	@Override
	public ActionState handleModifyEvent() {
		logger.debug("Modify Request accepted: State changed from Initial to Modify.");
		return new ModifyState();
		
	}

	@Override
	public void execute(Path filePath) throws NoSessionException, NoPeerConnectionException,
			IllegalFileLocation {
		logger.debug("Execute method in Initial State not defined.");
	}
}

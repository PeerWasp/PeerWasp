package org.peerbox.watchservice;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteState implements ActionState {
	
	private final static Logger logger = LoggerFactory.getLogger(DeleteState.class);
	
	@Override
	public ActionState handleCreateEvent() {
		System.out.println("Create Request accepted: Move detected.");
		return new MoveState();
	}

	@Override
	public ActionState handleDeleteEvent() {
		logger.debug("Delete Request denied: Already in Delete State.");
		return new DeleteState();
	}

	@Override
	public ActionState handleModifyEvent() {
		logger.debug("Modify Request denied: Cannot change from Delete to Modify State.");
		return new DeleteState();
	}
	
	
	@Override
	public void execute(File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("Delete State: Execute H2H \"Delete File\" API call");
		H2HManager manager = new H2HManager();
		IFileManager fileHandler = manager.getNode().getFileManager();
		
		fileHandler.delete(file);
		logger.debug("Task \"Delete File\" executed.");
	}
}

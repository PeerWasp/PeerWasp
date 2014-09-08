package org.peerbox.watchservice;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the delete state handles all events which would like 
 * to alter the state from "delete" to another state (or keep the current state) and decides
 * whether an transition into another state is allowed. 
 * 
 * 
 * @author winzenried
 *
 */
public class DeleteState implements ActionState {
	
	private final static Logger logger = LoggerFactory.getLogger(DeleteState.class);
	
	/**
	 * If a Create event is detected while the object is in the Delete state,
	 * one can assume that it is actually a move (or renaming) event
	 * 
	 * @return new MoveState object
	 */
	@Override
	public ActionState handleCreateEvent() {
		System.out.println("Create Request accepted: Move detected.");
		return new InitialState();
	}

	/**
	 * State is already set to Delete, therefore this event does not change the current state
	 * 
	 * @return new DeleteState object
	 */
	@Override
	public ActionState handleDeleteEvent() {
		logger.debug("Delete Request denied: Already in Delete State.");
		return new DeleteState();
	}

	/**
	 * After deleting a file, any modify action is not possible and cannot be
	 * accepted.
	 * 
	 * @return new DeleteState object
	 */
	@Override
	public ActionState handleModifyEvent() {
		logger.debug("Modify Request denied: Cannot change from Delete to Modify State.");
		//return new DeleteState();
		throw new IllegalStateTransissionException();
	}
	
	/**
	 * If the delete state is considered as stable, the execute method will be invoked which eventually
	 * deletes the file with the corresponding Hive2Hive method
	 * 
	 * @param file The file which should be deleted
	 */
	@Override
	public void execute(Path filePath) throws NoSessionException, NoPeerConnectionException {
		logger.debug("Delete State: Execute H2H \"Delete File\" API call");
		H2HManager manager = new H2HManager();
		//IFileManager fileHandler = manager.getNode().getFileManager();
		
		//fileHandler.delete(filePath.toFile());
		logger.debug("Task \"Delete File\" executed.");
	}
	
	@Override
	public ActionState handleMoveEvent(Path oldFilePath) {
		throw new RuntimeException("Not implemented...");
	}
}

package org.peerbox.watchservice;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * if a move or renaming (which actually is a move at the same path location) occurs,
 * this move state will be assigned. The transition to another state except the delete state
 * will not be accepted.
 * 
 * @author winzenried
 *
 */
public class MoveState implements ActionState {
	
	private final static Logger logger = LoggerFactory.getLogger(MoveState.class);
	
	/**
	 * The transition from Move to Create is not possible and will be denied
	 * 
	 * @return new MoveState object
	 */
	@Override
	public ActionState handleCreateEvent() {
		logger.debug("Create Request denied: Cannot change from Move to Create.");
		return new MoveState();
	}

	/**
	 * If an object gets deleted directly after it has been moved/renamed, the state
	 * changes to Delete
	 * 
	 * @return new DeleteState object
	 */
	@Override
	public ActionState handleDeleteEvent() {
		logger.debug("Delete Request accepted: State changed from Move to Delete.");
		return new DeleteState();
	}

	/**
	 * The state transition to Modify is not allowed
	 * 
	 * @return new MoveState object
	 */
	@Override
	public ActionState handleModifyEvent() {
		logger.debug("Modify Request denied: Cannot change from Move to Modify State.");
		return new MoveState();
	}
	
	@Override
	public void execute(File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("Move State: Execute \"Move File\" H2H API call");
		H2HManager manager = new H2HManager();
		IFileManager fileHandler = manager.getNode().getFileManager();
		
		//H2H move needs to be analyzed to make sure how it works
		fileHandler.move(file,file);
		logger.debug("Task \"Add File\" executed.");		
	}


}

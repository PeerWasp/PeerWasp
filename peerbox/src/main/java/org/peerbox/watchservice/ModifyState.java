package org.peerbox.watchservice;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.FileManager;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the modify state handles all events which would like 
 * to alter the state from Modify to another state (or keep the current state) and decides
 * whether an transition into another state is allowed. 
 * 
 * 
 * @author winzenried
 *
 */
public class ModifyState extends ActionState {
		
	private final static Logger logger = LoggerFactory.getLogger(ModifyState.class);
	
	public ModifyState(Action action) {
		super(action);
	}
	
	/**
	 * The transition from Modify to Create is not allowed
	 * 
	 * @return new ModifyState object
	 */
	@Override
	public ActionState handleCreateEvent() {
		logger.debug("Create Request denied: Cannot change from Modify to Create State.");
		throw new IllegalStateException("Create Request denied: Cannot change from Modify to Create State.");
	}

	/**
	 * A state transition from Modify to Delete is valid
	 * 
	 * @return new DeleteState object
	 */
	@Override
	public ActionState handleDeleteEvent() {
		logger.debug("Delete Request accepted: State changed from Modify to Delete.");
		return new DeleteState(action);
	}

	@Override
	public ActionState handleModifyEvent() {
		logger.debug("Modify Request denied: Already in Modify State.");
		return this;
	}
	
	@Override
	public ActionState handleMoveEvent(Path oldFilePath) {
		throw new RuntimeException("Not implemented...");
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException, IllegalArgumentException, NoPeerConnectionException {
		fileManager.update(action.getFilePath().toFile());
		logger.debug("Task \"Update File\" executed.");
	}
}

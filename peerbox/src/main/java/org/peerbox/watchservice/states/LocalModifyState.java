package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
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
public class LocalModifyState extends AbstractActionState {
		
	private final static Logger logger = LoggerFactory.getLogger(LocalModifyState.class);
	
	public LocalModifyState(Action action) {
		super(action);
	}
	
	/**
	 * The transition from Modify to Create is not allowed
	 * 
	 * @return new ModifyState object
	 */
	@Override
	public AbstractActionState handleLocalCreateEvent() {
		logger.debug("Create Request denied: Cannot change from Modify to Create State.");
		throw new IllegalStateException("Create Request denied: Cannot change from Modify to Create State.");
	}

	/**
	 * A state transition from Modify to Delete is valid
	 * 
	 * @return new DeleteState object
	 */
	@Override
	public AbstractActionState handleLocalDeleteEvent() {
		logger.debug("Delete Request accepted: State changed from Modify to Delete.");
		return new LocalDeleteState(action);
	}

	@Override
	public AbstractActionState handleLocalModifyEvent() {
		logger.debug("Modify Request denied: Already in Modify State.");
		return this;
	}
	
	@Override
	public AbstractActionState handleLocalMoveEvent(Path oldFilePath) {
		throw new RuntimeException("Not implemented...");
	}

	@Override
	public AbstractActionState handleRemoteCreateEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleRemoteDeleteEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleRemoteModifyEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleRemoteMoveEvent(Path oldFilePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException, IllegalArgumentException, NoPeerConnectionException {
		fileManager.update(action.getFilePath().toFile());
		logger.debug("Task \"Update File\" executed.");
	}
}

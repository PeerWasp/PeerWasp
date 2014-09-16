package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the Inital state is given when a file is considered as new and is not yet
 * uploaded (i.e. not available in DHT) - the transition to another state
 * is always valid and will be therefore accepted.
 * 
 * 
 * @author winzenried
 *
 */
public class InitialState extends ActionState {

	private final static Logger logger = LoggerFactory.getLogger(InitialState.class);
	
	public InitialState(Action action) {
		super(action);
	}
	
	/**
	 * The transition from Initial to Create will always be granted
	 * 
	 * @return new CreateState object
	 */
	@Override
	public ActionState handleLocalCreateEvent() {
		logger.debug("Create Request accepted: State changed from Initial to Create.");
		return new LocalCreateState(action);
	}

	/**
	 * The transition from Initial to Delete will always be granted
	 * 
	 * @return new DeleteState object
	 */
	@Override
	public ActionState handleLocalDeleteEvent() {
		logger.debug("Delete Request accepted: State changed from Initial to Delete.");
		return new LocalDeleteState(action);
	}

	/**
	 * The transition from Initial to Modify will always be granted
	 * 
	 * @return new ModifyState object
	 */
	@Override
	public ActionState handleLocalModifyEvent() {
		logger.debug("Modify Request accepted: State changed from Initial to Modify.");
		return new LocalModifyState(action);
		
	}

	@Override
	public ActionState handleLocalMoveEvent(Path oldFilePath) {
		return new LocalMoveState(action, oldFilePath);
		//throw new RuntimeException("Not implemented...");
	}

	/**
	 * There is no execution method for a file which is currently in the initial state
	 * 
	 */
	@Override
	public void execute(FileManager fileManager) throws NoSessionException, NoPeerConnectionException,
			IllegalFileLocation {
		logger.debug("Execute method in Initial State not defined.");
		//throw new RuntimeException("Not implemented...");
		
	}
}

package org.peerbox.watchservice;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the create state handles all events which would like 
 * to alter the state from "create" to another state (or keep the current state) and decides
 * whether an transition into another state is allowed. 
 * 
 * 
 * @author winzenried
 *
 */

public class CreateState implements ActionState {

	private final static Logger logger = LoggerFactory.getLogger(CreateState.class);
	
	public CreateState(){

	}
	
	/**
	 * State is already set to Create, therefore this event does not change the current state
	 * 
	 * @return a new CreateState object
	 */
	@Override
	public ActionState handleCreateEvent() {
		logger.debug("Create Request denied: Already in Create State.");
		//return new CreateState();
		throw new IllegalStateTransissionException();
	}

	/**
	 * Delete events will be accepted, however the state will change to Initial
	 * since any previous create action will not be excuted and therefore there is
	 * nothing to delete
	 * 
	 * @return new InitialState object
	 */
	@Override
	public ActionState handleDeleteEvent() {
		logger.debug("Delete Request accepted: State changed from Create to Initial.");
		return new InitialState();
	}

	/**
	 * The transition from Create to Modify is valid, but since any preceding create action
	 * was not yet executed, a modified file will be considered as "new" and therefore handled
	 * as a create event
	 * 
	 * @return new creatState object
	 */
	@Override
	public ActionState handleModifyEvent() {
		logger.debug("Modify Request accepted: State changed from Create to Modify.");
		return new CreateState();
		
	}
	
	/**
	 * If the create state is considered as stable, the execute method will be invoked which eventually
	 * uploads the file with the corresponding Hive2Hive method
	 * 
	 * @param file The file which should be uploaded
	 */
	@Override
	public void execute(Path filePath) throws NoSessionException, NoPeerConnectionException, IllegalFileLocation {
		logger.debug("Create State: Execute H2H \"Add File\" API call");
		//H2HManager manager = new H2HManager();
		//IFileManager fileHandler = manager.getNode().getFileManager();
		
		//fileHandler.add(filePath.toFile());
		logger.debug("Task \"Add File\" executed.");
	}
}

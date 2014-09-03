package org.peerbox.watchservice;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the modify state handles all events which would like 
 * to alter the state from "modify" to another state (or keep the current state) and decides
 * whether an transition into another state is allowed. 
 * 
 * 
 * @author winzenried
 *
 */
public class ModifyState implements ActionState {
		
	private final static Logger logger = LoggerFactory.getLogger(ModifyState.class);
	
		@Override
		public ActionState handleCreateEvent() {
			logger.debug("Create Request denied: Cannot change from Modify to Create State.");
			return new ModifyState();
		}

		@Override
		public ActionState handleDeleteEvent() {
			logger.debug("Delete Request accepted: State changed from Modify to Delete.");
			return new DeleteState();
		}

		@Override
		public ActionState handleModifyEvent() {
			logger.debug("Modify Request denied: Already in Modify State.");
			return new ModifyState();
			
		}
		
		@Override
		public void execute(File file) throws NoSessionException, IllegalArgumentException, NoPeerConnectionException {
			logger.debug("Modify State: Execute H2H \"Modify File\" API call");
			H2HManager manager = new H2HManager();
			IFileManager fileHandler = manager.getNode().getFileManager();
			
			fileHandler.update(file);
			logger.debug("Task \"Update File\" executed.");
		}
}

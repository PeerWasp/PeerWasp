package org.peerbox.watchservice.states;

import java.io.File;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
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

public class LocalCreateState extends AbstractActionState {
	private final static Logger logger = LoggerFactory.getLogger(LocalCreateState.class);

	public LocalCreateState(Action action) {
		super(action);
	}

	@Override
	public AbstractActionState handleLocalCreateEvent() {
		logger.debug("Local Create Event: Stay in Local Create");
		return this;
	}

	@Override
	public AbstractActionState handleLocalDeleteEvent() {
		logger.debug("Local Delete Event: Local Create -> Initial");
		return new InitialState(action);
	}

	@Override
	public AbstractActionState handleLocalUpdateEvent() {
		logger.debug("Local Update Event: Stay in Local Create");
		return this;
	}

	@Override
	public AbstractActionState handleLocalMoveEvent(Path oldFilePath) {
		logger.debug("Local Move Event: not defined");
		throw new IllegalStateException("Local Move Event: not defined");
	}

	@Override
	public AbstractActionState handleRemoteCreateEvent() {
		logger.debug("Remote Create Event: Local Create -> Conflict");
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState handleRemoteDeleteEvent() {
		logger.debug("Remote Delete Event: Local Create -> Conflict");
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState handleRemoteUpdateEvent() {
		logger.debug("Remote Update Event: Local Create -> Exception");
		return new ExceptionState(action);
	}

	@Override
	public AbstractActionState handleRemoteMoveEvent(Path oldFilePath) {
		logger.debug("Remote Move Event: Local Create -> Exception");
		return new ExceptionState(action);
	}

	/**
	 * If the create state is considered as stable, the execute method will be invoked which eventually
	 * uploads the file with the corresponding Hive2Hive method
	 * 
	 * @param file The file which should be uploaded
	 */
	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation {
		Path filePath = action.getFilePath();
		File file = filePath.toFile();
		fileManager.add(file);

		logger.debug("Task \"Add File\" executed.");
	}
}

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
 * the Initial state is given when a file is considered as new, synced or unknown.
 * The transition to another state is always valid and will be therefore accepted.
 * 
 * @author winzenried
 *
 */
public class InitialState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(InitialState.class);

	public InitialState(Action action) {
		super(action);
	}

	@Override
	public AbstractActionState handleLocalCreateEvent() {
		logger.debug("Local Create Event: Initial -> Local Create");
		return new LocalCreateState(action);
	}

	@Override
	public AbstractActionState handleLocalDeleteEvent() {
		logger.debug("Local Delete Event: Initial -> Local Delete");
		return new LocalDeleteState(action);
	}

	@Override
	public AbstractActionState handleLocalUpdateEvent() {
		logger.debug("Local Update Event: Initial -> Local Update");
		return new LocalUpdateState(action);

	}

	@Override
	public AbstractActionState handleLocalMoveEvent(Path oldFilePath, boolean isReversed) {
		logger.debug("Local Move Event: Initial -> Local Move");
		return new LocalMoveState(action, oldFilePath);
	}

	@Override
	public AbstractActionState handleRemoteCreateEvent() {
		logger.debug("Remote Create Event: Initial -> Remote Create");
		return new RemoteCreateState(action);
	}

	@Override
	public AbstractActionState handleRemoteDeleteEvent() {
		logger.debug("Remote Delete Event: Initial -> Remote Delete");
		return new RemoteDeleteState(action);
	}

	@Override
	public AbstractActionState handleRemoteUpdateEvent() {
		logger.debug("Remote Update Event: Initial -> Remote Update");
		return new RemoteUpdateState(action);
	}

	@Override
	public AbstractActionState handleRemoteMoveEvent(Path oldFilePath) {
		logger.debug("Remote Move Event: Initial -> Remote Move");
		return new RemoteMoveState(action);
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation {
		logger.warn("Execute method in Initial State not defined.");
	}
}

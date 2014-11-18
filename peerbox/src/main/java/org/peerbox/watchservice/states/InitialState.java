package org.peerbox.watchservice.states;

import java.io.IOException;
import java.nio.file.Files;
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
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event: Initial -> Local Create ({})", action.getFilePath());
		return new LocalCreateState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Initial -> Local Update ({})", action.getFilePath());
		return new LocalUpdateState(action);
	
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Local Delete Event: Initial -> Local Delete ({})", action.getFilePath());
		return new LocalDeleteState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldFilePath) {
		logger.debug("Local Move Event: Initial -> Local Move ({})", action.getFilePath());
		return new LocalMoveState(action, oldFilePath);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event: Initial -> Remote Update ({})", action.getFilePath());
		return new RemoteUpdateState(action);
	}
	
	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		logger.debug("Remote Update Event: Initial -> Remote Create ({})", action.getFilePath());
		return new RemoteCreateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event: Initial -> Remote Delete ({})", action.getFilePath());
		return new RemoteDeleteState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Initial -> Remote Move ({}) {}", action.getFilePath(), action.hashCode());
		
		Path path = action.getFilePath();
		logger.debug("Execute REMOTE MOVE: {}", path);
		try {
			Files.move(oldFilePath, path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new RemoteMoveState(action, oldFilePath);
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation {
		logger.warn("Execute is not defined in the initial state  ({})", action.getFilePath());
		notifyActionExecuteSucceeded();
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int versionToRecover) {
		logger.debug("Recover Event: Initial -> Recover ({})", action.getFilePath());
		return new RecoverState(action, versionToRecover);
	}

	@Override
	public void handleLocalCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleLocalDelete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleLocalUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleLocalMove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleLocalRecover() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteDelete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteMove() {
		// TODO Auto-generated method stub
		
	}
}

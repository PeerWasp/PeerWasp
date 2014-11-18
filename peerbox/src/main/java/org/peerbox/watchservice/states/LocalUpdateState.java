package org.peerbox.watchservice.states;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.ConflictHandler;
import org.peerbox.watchservice.states.AbstractActionState.FileManagerProcessListener;
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
public class LocalUpdateState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(LocalUpdateState.class);

	public LocalUpdateState(Action action) {
		super(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event: Stay in Local Update ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Local Delete Event: Local Update -> Local Delete ({})", action.getFilePath());
		return new LocalDeleteState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Stay in Local Update ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldFilePath) {
		logger.debug("Local Move Event: not defined");
		throw new IllegalStateException("Local Move Event: not defined");
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event: Local Update -> Conflict ({})", action.getFilePath());
		
		Path fileInConflict = action.getFilePath();
		Path renamedFile = ConflictHandler.rename(fileInConflict);
		try {
			Files.move(fileInConflict, renamedFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileInConflict = renamedFile;
		logger.debug("Conflict handling complete.");
		
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event: Local Update -> Conflict ({})", action.getFilePath());
		
		Path fileInConflict = action.getFilePath();
		Path renamedFile = ConflictHandler.rename(fileInConflict);
		try {
			Files.move(fileInConflict, renamedFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileInConflict = renamedFile;
		logger.debug("Conflict handling complete.");
		
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Local Update -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			IllegalArgumentException, NoPeerConnectionException, InvalidProcessStateException {
		Path path = action.getFilePath();
		logger.debug("Execute LOCAL UPDATE: {}", path);
		IProcessComponent process = fileManager.update(path.toFile());
		process.attachListener(new FileManagerProcessListener());
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int versionToRecover) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		// TODO Auto-generated method stub
		return new ConflictState(action);
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

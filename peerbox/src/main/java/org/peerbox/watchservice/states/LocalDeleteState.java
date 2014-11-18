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
 * the delete state handles all events which would like
 * to alter the state from "delete" to another state (or keep the current state) and decides
 * whether an transition into another state is allowed.
 * 
 * 
 * @author winzenried
 *
 */
public class LocalDeleteState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(LocalDeleteState.class);

	public LocalDeleteState(Action action) {
		super(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event: Local Delete -> Local Update ({})", action.getFilePath());
		return new LocalUpdateState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Local Delete Event: Stay in Local Delete ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Stay in Local Delete ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldFilePath) {
		logger.debug("Local Move Event: Delete -> Local Move ({})", action.getFilePath());
		return new LocalMoveState(action, oldFilePath);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event: Local Delete -> Conflict ({})", action.getFilePath());
		
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
		logger.debug("Remote Delete Event: Local Delete -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Local Delete -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	/**
	 * If the delete state is considered as stable, the execute method will be invoked which eventually
	 * deletes the file with the corresponding Hive2Hive method
	 * 
	 * @param file The file which should be deleted
	 * @throws InvalidProcessStateException 
	 */
	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException {
		Path path = action.getFilePath();
		logger.debug("Execute LOCAL DELETE: {}", path);
		IProcessComponent process = fileManager.delete(path.toFile());
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
		return this;
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

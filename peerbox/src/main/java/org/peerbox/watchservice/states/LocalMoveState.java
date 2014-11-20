package org.peerbox.watchservice.states;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.lang3.NotImplementedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * if a move or renaming (which actually is a move at the same path location) occurs,
 * this move state will be assigned. The transition to another state except the delete state
 * will not be accepted.
 * 
 * @author winzenried
 *
 */
public class LocalMoveState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(LocalMoveState.class);

	private Path sourcePath;
	private boolean reversePaths;

	public LocalMoveState(Action action, Path sourcePath) {
		super(action);
		this.sourcePath = sourcePath;
		reversePaths = false;
	}
	
	public LocalMoveState(Action action, Path sourcePath, boolean reversePaths) {
		super(action);
		this.sourcePath = sourcePath;
		this.reversePaths = reversePaths;
	}


	public Path getSourcePath() {
		return sourcePath;
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event: not defined ({})", action.getFilePath());
//		throw new IllegalStateException("Local Create Event: not defined");
		return new InitialState(action);
	}

	// TODO Needs to be verified (Patrick, 21.10.14)
	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Local Move -> Local Update ({})", action.getFilePath());
//		throw new IllegalStateException("Local Update Event: not defined");
		return new LocalUpdateState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Local Delete Event: not defined ({})", action.getFilePath());
//		throw new IllegalStateException("Local Delete Event: not defined");
		return new InitialState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldPath) {
		logger.debug("Local Move Event: not defined ({})", action.getFilePath());
//		throw new IllegalStateException("Local Move Event: not defined");
		return new InitialState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event: Local Move -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event: Local Move -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Local Move -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException {
	
		try {
			IProcessComponent process = fileManager.move(sourcePath.toFile(), action.getFilePath().toFile());
			if(process != null){
				process.attachListener(new FileManagerProcessListener());
			}
			
		} catch (InvalidProcessStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("Task \"Move File\" executed from: " + sourcePath.toString() + " to " + action.getFilePath().toFile().toPath());
//		notifyActionExecuteSucceeded();
		
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
	public AbstractActionState handleLocalCreate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalMoveState.handleLocalRecover");
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalMoveState.handleLocalRecover");
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalMoveState.handleLocalUpdate");
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalMoveState.handleLocalMove");
	}

	@Override
	public AbstractActionState handleLocalRecover(int version) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalMoveState.handleLocalRecover");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalMoveState.handleRemoteCreate");
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalMoveState.handleRemoteDelete");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalMoveState.handleRemoteUpdate");
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalMoveState.handleRemoteMove");
	}
}

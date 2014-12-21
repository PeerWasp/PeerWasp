package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.FileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.h2h.ProcessHandle;
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
		super(action, StateType.LOCAL_MOVE);
		this.sourcePath = sourcePath;
		reversePaths = false;
	}
	
	public LocalMoveState(Action action, Path sourcePath, boolean reversePaths) {
		super(action, StateType.LOCAL_MOVE);
		this.sourcePath = sourcePath;
		this.reversePaths = reversePaths;
	}


	public Path getSourcePath() {
		return sourcePath;
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		return new InitialState(action); //Move is applied to source files, this is the destination, hence the event is ignored
	}

	// TODO Needs to be verified (Patrick, 21.10.14)
	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Local Move -> Local Update ({})", action.getFilePath());

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
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException, NoPeerConnectionException, ProcessExecutionException, InvalidProcessStateException {
	
		ProcessHandle<Void> handle = fileManager.move(sourcePath.toFile(), action.getFilePath().toFile());
		if(handle != null){
			handle.getProcess().attachListener(new FileManagerProcessListener());
			handle.executeAsync();
		}
			
		logger.debug("Task \"Move File\" executed from: " + sourcePath.toString() + " to " + action.getFilePath().toFile().toPath());
//		notifyActionExecuteSucceeded();
		return new ExecutionHandle(action, handle);
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		throw new NotImplException("LocalMoveState.handleLocalRecover");
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		throw new NotImplException("LocalMoveState.handleLocalRecover");
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		throw new NotImplException("LocalMoveState.handleLocalUpdate");
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		throw new NotImplException("LocalMoveState.handleLocalMove");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		throw new NotImplException("LocalMoveState.handleRemoteCreate");
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		throw new NotImplException("LocalMoveState.handleRemoteDelete");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		throw new NotImplException("LocalMoveState.handleRemoteUpdate");
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		throw new NotImplException("LocalMoveState.handleRemoteMove");
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleLocalRecover(int version) {
		// TODO Auto-generated method stub
		throw new NotImplException("LocalRecoverState.handleLocalRecover");
	}
}

package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.FileManager;
import org.peerbox.exceptions.NotImplException;
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

	private Path source;

	public LocalMoveState(Action action, Path source) {
		super(action, StateType.LOCAL_MOVE);
		this.source = source;
	}

	public Path getSourcePath() {
		return source;
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
	public AbstractActionState changeStateOnLocalMove(Path destination) {
		logger.debug("Local Move Event: not defined ({})", action.getFilePath());
//		throw new IllegalStateException("Local Move Event: not defined");
		return new InitialState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event: Local Move -> Conflict ({})", action.getFilePath());
		throw new NotImplException("Conflict handling during move not yet supported");
//		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event: Local Move -> Local Create ({})", action.getFilePath());
		return new LocalCreateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Local Move -> Conflict ({})", action.getFilePath());
		throw new NotImplException("Conflict handling during move not yet supported");
//		return new ConflictState(action);
	}

	@Override
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException, NoPeerConnectionException, ProcessExecutionException, InvalidProcessStateException {
	
		handle = fileManager.move(source.toFile(), action.getFilePath().toFile());
		if(handle != null){
			handle.getProcess().attachListener(new FileManagerProcessListener());
			handle.executeAsync();
		}
			
		logger.debug("Task \"Move File\" executed from: " + source.toString()  + " to " + action.getFilePath().toFile().toPath() );
//		notifyActionExecuteSucceeded();
		return new ExecutionHandle(action, handle);
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		throw new NotImplException("Conflict handling during move not yet supported");
//		return new ConflictState(action);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		updateTimeAndQueue();
		return this;
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
		updateTimeAndQueue();
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		updateTimeAndQueue();
		return changeStateOnRemoteDelete();
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		updateTimeAndQueue();
		return changeStateOnRemoteUpdate();
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		throw new NotImplException("LocalMoveState.handleRemoteMove");
	}
}

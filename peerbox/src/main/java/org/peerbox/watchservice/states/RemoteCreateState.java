package org.peerbox.watchservice.states;

import java.io.File;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.h2h.ProcessHandle;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteCreateState extends AbstractActionState {

	private static final Logger logger = LoggerFactory.getLogger(RemoteCreateState.class);

	public RemoteCreateState(Action action) {
		super(action, StateType.REMOTE_CREATE);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event in RemoteCreateState!  ({}) {}", 
				action.getFilePath(), action.hashCode());

		action.getFile().bubbleContentHashUpdate();//updateContentHash();
		return new EstablishedState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event:  ({})", action.getFilePath());
		ConflictHandler.resolveConflict(action.getFilePath());
		return new LocalUpdateState(action);
//		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		return new InitialState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldPath) {
		throw new IllegalStateException("Local move in RemoteCreateState");
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event:  ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event:  ({})", action.getFilePath());
		return new InitialState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		throw new IllegalStateException("Remote Move eent in RemoteCreateState");
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		return this;
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		action.getFile().bubbleContentHashUpdate();//updateContentHash();
		return changeStateOnLocalCreate();
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		return changeStateOnLocalDelete();
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		return changeStateOnLocalUpdate();
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		return changeStateOnLocalMove(oldPath);
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		return changeStateOnRemoteDelete();
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		throw new NotImplException("RemoteCreateState.handleRemoteUpdate");
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		return changeStateOnRemoteMove(path);
	}

	@Override
	public ExecutionHandle execute(FileManager fileManager) throws InvalidProcessStateException,
			ProcessExecutionException, NoSessionException, NoPeerConnectionException {
		Path path = action.getFilePath();
		logger.debug("Execute REMOTE ADD, download the file: {}", path);
		handle = fileManager.download(path.toFile());
		if (handle != null && handle.getProcess() != null) {
			handle.getProcess().attachListener(new FileManagerProcessListener());
			handle.executeAsync();
		} else {
			System.err.println("process or handle is null");
		}
		return new ExecutionHandle(action, handle);
	}

}

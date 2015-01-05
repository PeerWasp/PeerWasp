package org.peerbox.watchservice.states;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
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

import com.google.common.io.Files;

public class RemoteUpdateState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(RemoteUpdateState.class);

	public RemoteUpdateState(Action action) {
		super(action, StateType.REMOTE_UPDATE);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event in RemoteUpdateState!  ({})", action.getFilePath());
		logStateTransission(getStateType(), EventType.LOCAL_CREATE, StateType.REMOTE_UPDATE);
		return new LocalUpdateState(action);//new EstablishedState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logStateTransission(getStateType(), EventType.LOCAL_UPDATE, StateType.REMOTE_UPDATE);
		return new LocalUpdateState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logStateTransission(getStateType(), EventType.LOCAL_DELETE, StateType.REMOTE_UPDATE);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path newPath) {
		logger.debug("Cannot accept local move right now, since update is happening.");
		logStateTransission(getStateType(), EventType.LOCAL_MOVE, getStateType());
		return new LocalMoveState(action, newPath);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event:  ({})", action.getFilePath());
		logStateTransission(getStateType(), EventType.REMOTE_UPDATE, StateType.REMOTE_UPDATE);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event:  ({})", action.getFilePath());
		throw new NotImplException("RemoteUpdate.remoteDelete");
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event:  ({})", action.getFilePath());
		throw new NotImplException("RemoteUpdate.remoteMove");
	}
	
	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		return this;
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		return changeStateOnLocalCreate();
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		action.getFile().bubbleContentHashUpdate();
		return changeStateOnLocalUpdate();
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		
		action.getNextState().changeStateOnRemoteUpdate();
		return changeStateOnLocalMove(oldPath);
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		throw new NotImplException("RemoteUpdateState.handleRemoteDelete");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		return changeStateOnRemoteUpdate();
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		throw new NotImplException("RemoteUpdateState.handleRemoteMove");
	}

	@Override
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		Path path = action.getFilePath();
		logger.debug("Execute REMOTE UPDATE, download the file: {}", path);

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

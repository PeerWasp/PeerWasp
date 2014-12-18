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

public class RemoteUpdateState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(RemoteUpdateState.class);

	public RemoteUpdateState(Action action) {
		super(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event in RemoteUpdateState!  ({})", action.getFilePath());
		logStateTransission(getStateType(), EventType.LOCAL_CREATE, StateType.REMOTE_UPDATE);
		return this;//new EstablishedState(action);
		//TODO: maybe we should return 'this.' as soon as the update is successful, transission into ESTABLISHED happens!
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logStateTransission(getStateType(), EventType.LOCAL_UPDATE, StateType.REMOTE_UPDATE);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logStateTransission(getStateType(), EventType.LOCAL_DELETE, StateType.REMOTE_UPDATE);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldPath) {
		logger.debug("Local Move Event:  ({})", action.getFilePath());
		throw new NotImplException("RemoteUpdateState.LocalMove");
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

//	@Override
//	public AbstractActionState getDefaultState(){
//		return this;
//	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
//		throw new NotImplException("RemoteUpdateState.handleLocalCreate");
		return changeStateOnLocalCreate();
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		return changeStateOnLocalDelete();
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
//		throw new NotImplException("RemoteUpdateState.handleLocalUpdate");
		action.getFile().bubbleContentHashUpdate();
		return changeStateOnLocalUpdate();
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		throw new NotImplException("RemoteUpdateState.handleLocalMove");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		throw new NotImplException("RemoteUpdateState.handleRemoteCreate");
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		throw new NotImplException("RemoteUpdateState.handleRemoteDelete");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		throw new NotImplException("RemoteUpdateState.handleRemoteUpdate");
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		throw new NotImplException("RemoteUpdateState.handleRemoteMove");
	}

	@Override
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		Path path = action.getFilePath();
		logger.debug("Execute REMOTE UPDATE, download the file: {}", path);
		ProcessHandle<Void> handle = fileManager.download(path.toFile());
		if (handle != null && handle.getProcess() != null) {
			handle.getProcess().attachListener(new FileManagerProcessListener());
			handle.executeAsync();
		} else {
			System.err.println("process or handle is null");
		}

		return new ExecutionHandle(action, handle);
		// notifyActionExecuteSucceeded();
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleLocalRecover(int version) {
		// TODO Auto-generated method stub
		throw new NotImplException("RemoteUpdateState.handleLocalRecover");
	}
}

package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.presenter.settings.synchronization.FileHelper;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.peerbox.watchservice.states.listeners.RemoteFileAddListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteCreateState extends AbstractActionState {

	private static final Logger logger = LoggerFactory.getLogger(RemoteCreateState.class);

	private boolean localCreateHappened = false;
	public RemoteCreateState(IAction action) {
		super(action, StateType.REMOTE_CREATE);
	}
	
	@Override
	public ExecutionHandle execute(IFileManager fileManager) throws InvalidProcessStateException,
			ProcessExecutionException, NoSessionException, NoPeerConnectionException {
		final Path path = action.getFile().getPath();
		logger.debug("Execute REMOTE ADD, download the file: {}", path);
		handle = fileManager.download(path);
		if (handle != null && handle.getProcess() != null) {
			FileHelper file = new FileHelper(path, action.getFile().isFile());
			handle.getProcess().attachListener(new RemoteFileAddListener(file, action.getFileEventManager().getMessageBus()));
			handle.executeAsync();
		} else {
			System.err.println("process or handle is null");
		}
		return new ExecutionHandle(action, handle);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logStateTransition(getStateType(), EventType.LOCAL_CREATE, StateType.ESTABLISHED);
		return new EstablishedState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logStateTransition(getStateType(), EventType.REMOTE_UPDATE, StateType.REMOTE_CREATE);
		return this;
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		action.getFile().updateContentHash();
		return changeStateOnLocalCreate();
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		//TODO remove from queue?
		return changeStateOnLocalDelete();
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		ConflictHandler.resolveConflict(action.getFile().getPath());
		return changeStateOnLocalUpdate();
	}
	
	public AbstractActionState changeStateOnLocalUpdate(){
		logStateTransition(getStateType(), EventType.LOCAL_UPDATE, StateType.REMOTE_CREATE);
		return this;
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		//TODO: Remove from queue /tree
		return changeStateOnRemoteDelete();
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		return changeStateOnRemoteMove(path);
	}
	
	public boolean localCreateHappened(){
		return localCreateHappened;
	}

	public void setLocalCreateHappened(boolean b) {
		localCreateHappened = b;
	}

}

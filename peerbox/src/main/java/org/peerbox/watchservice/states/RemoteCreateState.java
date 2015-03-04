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
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
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
		return changeStateOnLocalDelete();
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		ConflictHandler.resolveConflict(action.getFile().getPath());
		updateTimeAndQueue();
		return changeStateOnLocalUpdate();
	}
	
	public AbstractActionState changeStateOnLocalUpdate(){
		logStateTransition(getStateType(), EventType.LOCAL_UPDATE, StateType.REMOTE_CREATE);
		return this;
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		logger.info("The file which was remotely moved after it has been "
				+ "remotely created. RemoteCreate at destination"
				+ "of move operation initiated to download the file: {}", path);
		updateTimeAndQueue();
		IFileTree fileTree = action.getFileEventManager().getFileTree();
		fileTree.deleteFile(action.getFile().getPath());
		action.getFileEventManager().getFileComponentQueue().remove(action.getFile());
		FileComponent moveDest = fileTree.getOrCreateFileComponent(path, action.getFileEventManager());
		fileTree.putFile(path, moveDest);
		moveDest.getAction().handleRemoteCreateEvent();

		return changeStateOnRemoteMove(path);
	}
	
	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logStateTransition(getStateType(), EventType.REMOTE_MOVE, StateType.INITIAL);
		return new InitialState(action);
	}
}

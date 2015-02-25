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
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.states.listeners.LocalFileAddListener;
import org.peerbox.watchservice.states.listeners.LocalFileDeleteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalHardDeleteState extends AbstractActionState{

	private final static Logger logger = LoggerFactory.getLogger(LocalHardDeleteState.class);

	public LocalHardDeleteState(IAction action) {
		super(action, StateType.LOCAL_HARD_DELETE);
	}

	public AbstractActionState getDefaultState() {
		logger.debug("Return to default state 'InitialState' as component was removed completely {}",
				action.getFile().getPath());
		return new InitialState(action);
	}
	
	@Override
	public ExecutionHandle execute(IFileManager fileManager) throws InvalidProcessStateException, ProcessExecutionException, NoSessionException, NoPeerConnectionException {
		final Path path = action.getFile().getPath();
		logger.debug("Execute LOCAL DELETE: {}", path);
		handle = fileManager.delete(path);
		if (handle != null && handle.getProcess() != null) {
			FileHelper file = new FileHelper(path, action.getFile().isFile());
			handle.getProcess().attachListener(new LocalFileDeleteListener(file, action.getFileEventManager().getMessageBus()));
			handle.executeAsync();
		} else {
			System.err.println("handle or process is null.");
		}

		return new ExecutionHandle(action, handle);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logStateTransition(getStateType(), EventType.LOCAL_CREATE, StateType.LOCAL_UPDATE);
		return new LocalUpdateState(action); //e.g. hard delete -> Ctrl + Z;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logStateTransition(getStateType(), EventType.LOCAL_DELETE, StateType.LOCAL_HARD_DELETE);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logStateTransition(getStateType(), EventType.REMOTE_UPDATE, StateType.REMOTE_UPDATE);
		return new RemoteCreateState(action); // The network wins
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logStateTransition(getStateType(), EventType.REMOTE_MOVE, StateType.LOCAL_HARD_DELETE);
		return this;
	}

	public AbstractActionState handleLocalDelete(){
		IFileEventManager eventManager = action.getFileEventManager();
		IFileTree fileTree = eventManager.getFileTree();
		
		fileTree.deleteFile(action.getFile().getPath());
		
		updateTimeAndQueue();
		return changeStateOnLocalDelete();
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		action.getFileEventManager().getFileComponentQueue().remove(action.getFile());
		return changeStateOnRemoteDelete();
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		logger.info("The file which was locally deleted has been moved remotely. RemoteCreate at destination"
				+ "of move operation initiated to download the file: {}", path);
		updateTimeAndQueue();
		IFileTree fileTree = action.getFileEventManager().getFileTree();
		FileComponent moveDest = fileTree.getOrCreateFileComponent(path, action.getFileEventManager());
		fileTree.putFile(path, moveDest);
		moveDest.getAction().handleRemoteCreateEvent();

		return changeStateOnRemoteMove(path);
	}

}

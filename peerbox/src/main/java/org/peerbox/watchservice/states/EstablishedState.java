package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EstablishedState extends AbstractActionState{

	private static final Logger logger = LoggerFactory.getLogger(EstablishedState.class);

	public EstablishedState(IAction action) {
		super(action, StateType.ESTABLISHED);
	}
	
	@Override
	public ExecutionHandle execute(IFileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException {
		logger.error("Execute in the ESTABLISHED state is only called due to wrong behaviour! {}", action.getFile().getPath());
		return null;
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logStateTransition(getStateType(), EventType.LOCAL_CREATE, StateType.ESTABLISHED);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		logStateTransition(getStateType(), EventType.REMOTE_CREATE, StateType.REMOTE_UPDATE);
		return new RemoteUpdateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logStateTransition(getStateType(), EventType.REMOTE_MOVE, StateType.ESTABLISHED);
		return this;
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		updateTimeAndQueue();
		action.getFile().updateContentHash();
		return changeStateOnLocalCreate();
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		updateTimeAndQueue();
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteMove(Path destPath) {
		final IFileEventManager eventManager = action.getFileEventManager();
		final IFileTree fileTree = eventManager.getFileTree();
		final FileComponent file = action.getFile();
		
		eventManager.getFileComponentQueue().remove(file);
		Path sourcePath = file.getPath();

		fileTree.deleteFile(file.getPath());
		fileTree.putFile(destPath, file);

		return changeStateOnRemoteMove(sourcePath);
	}

}

package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EstablishedState extends AbstractActionState{

	private static final Logger logger = LoggerFactory.getLogger(EstablishedState.class);

	public EstablishedState(IAction action) {
		super(action, StateType.ESTABLISHED);

	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logStateTransition(getStateType(), EventType.LOCAL_CREATE, StateType.ESTABLISHED);

		action.getFile().bubbleContentHashUpdate();
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
	public ExecutionHandle execute(IFileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException {
		logger.error("Execute in the ESTABLISHED state is only called due to wrong behaviour! {}", action.getFile().getPath());
		return null;
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		action.getFile().bubbleContentHashUpdate();
		return changeStateOnLocalCreate();
	}

//	@Override
//	public AbstractActionState handleLocalMove(Path newPath) {
//		action.getFileEventManager().getFileTree().putFile(newPath, action.getFile());
//		return changeStateOnLocalMove(newPath);
//	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		updateTimeAndQueue();
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteMove(Path dstPath) {
		action.getFileEventManager().getFileComponentQueue().remove(action.getFile());
		Path oldPath = action.getFile().getPath();
		logger.debug("Modify the tree accordingly. Src: {} Dst: {}", action.getFile().getPath(), dstPath);

		FileComponent deleted = action.getFileEventManager().getFileTree().deleteFile(action.getFile().getPath());
		action.getFileEventManager().getFileTree().putFile(dstPath, action.getFile());

		Path path = dstPath;
		logger.debug("Execute REMOTE MOVE: {}", path);

		AbstractActionState returnState = changeStateOnRemoteMove(oldPath);
		return returnState;
	}

}

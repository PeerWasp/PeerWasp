package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EstablishedState extends AbstractActionState{

	private static final Logger logger = LoggerFactory.getLogger(EstablishedState.class);

	public EstablishedState(Action action) {
		super(action, StateType.ESTABLISHED);

	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logStateTransission(getStateType(), EventType.LOCAL_CREATE, StateType.LOCAL_CREATE);

		action.getFile().bubbleContentHashUpdate();
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		logStateTransission(getStateType(), EventType.REMOTE_CREATE, StateType.REMOTE_UPDATE);
		return new RemoteUpdateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logStateTransission(getStateType(), EventType.REMOTE_MOVE, StateType.ESTABLISHED);
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

	@Override
	public AbstractActionState handleLocalMove(Path newPath) {
		action.getEventManager().getFileTree().putFile(newPath, action.getFile());
		return changeStateOnLocalMove(newPath);
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		updateTimeAndQueue();
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteMove(Path dstPath) {
		action.getEventManager().getFileComponentQueue().remove(action.getFile());
		Path oldPath = action.getFile().getPath();
		logger.debug("Modify the tree accordingly. Src: {} Dst: {}", action.getFile().getPath(), dstPath);

		FileComponent deleted = action.getEventManager().getFileTree().deleteFile(action.getFile().getPath());
		action.getEventManager().getFileTree().putFile(dstPath, action.getFile());

		Path path = dstPath;
		logger.debug("Execute REMOTE MOVE: {}", path);

		AbstractActionState returnState = changeStateOnRemoteMove(oldPath);
		return returnState;
	}

}

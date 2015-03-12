package org.peerbox.watchservice.states;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.states.listeners.LocalFileUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Files in the LocalUpdateState have been uploaded and synchronized with
 * the H2H network, but changed locally in the meantime. The change refers
 * always to the actual content of a file, and not to meta-data like timestamps, as
 * H2H only distinguishes between file versions if their content does not equal.
 *
 * Since folders only have one version in H2H, the LocalUpdateState is not reasonable
 * for folders. If a folder ends up in the LocalUpdateState for some reason and tries
 * to upload a new version of itself, the network will reject the request.
 *
 * @author claudio
 */
public class LocalUpdateState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(LocalUpdateState.class);

	public LocalUpdateState(IAction action) {
		super(action, StateType.LOCAL_UPDATE);
	}

	@Override
	public ExecutionHandle execute(IFileManager fileManager) throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		final Path path = action.getFile().getPath();
		logger.debug("Execute LOCAL UPDATE: {}", path);
		handle = fileManager.update(path);
		if (handle != null && handle.getProcess() != null) {
			FileInfo file = new FileInfo(path, action.getFile().isFolder());
			handle.getProcess().attachListener(new LocalFileUpdateListener(file, action.getFileEventManager().getMessageBus()));
			handle.executeAsync();
		} else {
			System.err.println("Process or handle is null.");
		}
		return new ExecutionHandle(action, handle);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logStateTransition(getStateType(), EventType.LOCAL_CREATE, StateType.LOCAL_UPDATE);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logStateTransition(getStateType(), EventType.REMOTE_DELETE, StateType.LOCAL_CREATE);
		return new LocalCreateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logStateTransition(getStateType(), EventType.REMOTE_MOVE, StateType.LOCAL_UPDATE);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		logStateTransition(getStateType(), EventType.REMOTE_CREATE, StateType.REMOTE_CREATE);
		return new RemoteCreateState(action);
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		ConflictHandler.resolveConflict(action.getFile().getPath());
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
		ConflictHandler.resolveConflict(action.getFile().getPath());
		updateTimeAndQueue();
		return changeStateOnRemoteUpdate();
	}

	//TODO write test-case for this!
	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		final IFileEventManager eventManager = action.getFileEventManager();
		final IFileTree fileTree = eventManager.getFileTree();
		final FileComponent file = action.getFile();

		eventManager.getFileComponentQueue().remove(file);
		Path sourcePath = file.getPath();

		fileTree.deleteFile(file.getPath());
		//TODO set path of file to path
		fileTree.putFile(path, file);
		updateTimeAndQueue();
		return changeStateOnRemoteMove(sourcePath);
	}

}

package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.states.listeners.LocalFileMoveListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Files in the LocalMove state have been locally moved. The file already exists
 * at the destination of the move operation, both in the file-system and in the
 * {@link org.peerbox.watchservice.filetree.FileTree FileTree} representing it.
 * The source path of the file is known and provided to the H2H network.
 * @author Claudio
 *
 */
public class LocalMoveState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(LocalMoveState.class);

	private Path source;

	public LocalMoveState(IAction action, Path source) {
		super(action, StateType.LOCAL_MOVE);
		this.source = source;
	}

	public Path getSourcePath() {
		return source;
	}

	@Override
	public ExecutionHandle execute(IFileManager fileManager) throws NoSessionException, NoPeerConnectionException, ProcessExecutionException, InvalidProcessStateException {

		final Path path = action.getFile().getPath();
		handle = fileManager.move(source, path);
		if(handle != null){
			FileInfo destFile = new FileInfo(action.getFile());
			FileInfo sourceFile = new FileInfo(source, action.getFile().isFolder());
			handle.getProcess().attachListener(new LocalFileMoveListener(sourceFile, destFile, action.getFileEventManager().getMessageBus()));
			handle.executeAsync();
		}

		String contentHash = action.getFile().getContentHash();
//		Path pathToRemove = action.getFile().getPath();
		IFileTree fileTree = action.getFileEventManager().getFileTree();
		boolean isRemoved = fileTree.getCreatedByContentHash().get(contentHash).remove(action.getFile());
		logger.trace("IsRemoved for file {} with hash {}: {}", action.getFile().getPath(), contentHash, isRemoved);


		logger.debug("Task \"Move File\" executed from: " + source.toString()  + " to " + path.toString());
		return new ExecutionHandle(action, handle);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logStateTransition(getStateType(), EventType.LOCAL_CREATE, StateType.INITIAL);
		return new InitialState(action);//Move is applied to source files, this is the destination, hence the event is ignored
	}

	// TODO Needs to be verified (Patrick, 21.10.14)
	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.trace("This state transition should not happe: {}.", action.getFile().getPath());
		logStateTransition(getStateType(), EventType.LOCAL_UPDATE, StateType.LOCAL_MOVE);
		return this; //new LocalMoveState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path destination) {
		logStateTransition(getStateType(), EventType.LOCAL_MOVE, StateType.INITIAL);
		return new InitialState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logStateTransition(getStateType(), EventType.REMOTE_UPDATE, StateType.REMOTE_UPDATE);
		return new EstablishedState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logStateTransition(getStateType(), EventType.REMOTE_DELETE, StateType.LOCAL_CREATE);
		return new EstablishedState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logStateTransition(getStateType(), EventType.REMOTE_MOVE, StateType.LOCAL_MOVE);
		return new EstablishedState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		logStateTransition(getStateType(), EventType.REMOTE_CREATE, StateType.REMOTE_CREATE);
		return new EstablishedState(action);
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
//		logger.info("The file which was locally moved to a place where the "
//				+ "same file was remotely created. RemoteCreate at destination of local "
//				+ "move operation initiated to download the file: {}", action.getFile().getPath());
//		updateTimeAndQueue();
//		IFileTree fileTree = action.getFileEventManager().getFileTree();
//
//		ConflictHandler.resolveConflict(action.getFile().getPath(), true);

//		action.getFileEventManager().initiateForceSync(action.getFile().getPath().getParent());
		action.getFileEventManager().initiateForceSync(action.getFile().getPath()
				.getParent());
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		action.updateTimeAndQueue();
		action.getFileEventManager().initiateForceSync(action.getFile().getPath()
				.getParent());
		return changeStateOnRemoteDelete();
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
//		logger.info("The file which was locally moved to a place where the "
//				+ "same file was remotely updated. RemoteUpdate at destination of local "
//				+ "move operation initiated to download the file: {}", action.getFile().getPath());
//		action.updateTimeAndQueue();
//		IFileTree fileTree = action.getFileEventManager().getFileTree();
//
//		ConflictHandler.resolveConflict(action.getFile().getPath(), true);
//
		action.getFileEventManager().initiateForceSync(action.getFile().getPath()
				.getParent());
		return changeStateOnRemoteUpdate();
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
//		logger.info("The file which was locally moved after it has been "
//				+ "remotely moved. RemoteCreate at destination of remote "
//				+ "move operation initiated to download the file: {}", path);
//		action.updateTimeAndQueue();
//		IFileTree fileTree = action.getFileEventManager().getFileTree();
//
//		FileComponent moveDest = fileTree.getOrCreateFileComponent(path, action.getFileEventManager());
//		//TODO set path of moveDest to path
//		fileTree.putFile(path, moveDest);
//		moveDest.getAction().handleRemoteCreateEvent();
		action.getFileEventManager().initiateForceSync(action.getFile().getPath()
				.getParent());
		return changeStateOnRemoteMove(path);
	}
}

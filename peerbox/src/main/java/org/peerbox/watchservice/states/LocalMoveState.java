package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.states.listeners.LocalFileAddListener;
import org.peerbox.watchservice.states.listeners.LocalFileMoveListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * if a move or renaming (which actually is a move at the same path location) occurs,
 * this move state will be assigned. The transition to another state except the delete state
 * will not be accepted.
 *
 * @author winzenried
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
	public AbstractActionState changeStateOnLocalCreate() {
		return new InitialState(action); //Move is applied to source files, this is the destination, hence the event is ignored
	}

	// TODO Needs to be verified (Patrick, 21.10.14)
	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Local Move -> Local Update ({})", action.getFile().getPath());

		return new LocalUpdateState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path destination) {
		logger.debug("Local Move Event: not defined ({})", action.getFile().getPath());
//		throw new IllegalStateException("Local Move Event: not defined");
		return new InitialState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event: Local Move -> Conflict ({})", action.getFile().getPath());
		throw new NotImplException("Conflict handling during move not yet supported");
//		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event: Local Move -> Local Create ({})", action.getFile().getPath());
		return new LocalCreateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Local Move -> Conflict ({})", action.getFile().getPath());
		throw new NotImplException("Conflict handling during move not yet supported");
//		return new ConflictState(action);
	}

	@Override
	public ExecutionHandle execute(IFileManager fileManager) throws NoSessionException, NoPeerConnectionException, ProcessExecutionException, InvalidProcessStateException {

		final Path path = action.getFile().getPath();
		handle = fileManager.move(source, path);
		if(handle != null){
			handle.getProcess().attachListener(new LocalFileMoveListener(path, action.getFileEventManager().getMessageBus()));
			handle.executeAsync();
		}

		String contentHash = action.getFile().getContentHash();
		Path pathToRemove = action.getFile().getPath();
		IFileTree fileTree = action.getFileEventManager().getFileTree();
		boolean isRemoved = fileTree.getCreatedByContentHash().get(contentHash).remove(action.getFile());
		logger.trace("IsRemoved for file {} with hash {}: {}", action.getFile().getPath(), contentHash, isRemoved);

		
		logger.debug("Task \"Move File\" executed from: " + source.toString()  + " to " + path.toString());
		return new ExecutionHandle(action, handle);
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		throw new NotImplException("Conflict handling during move not yet supported");
//		return new ConflictState(action);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		updateTimeAndQueue();
		return this;
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		throw new NotImplException("LocalMoveState.handleLocalUpdate");
	}

//	@Override
//	public AbstractActionState handleLocalMove(Path oldPath) {
//		throw new NotImplException("LocalMoveState.handleLocalMove");
//	}

	@Override
	public AbstractActionState handleRemoteCreate() {
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
		updateTimeAndQueue();
		return changeStateOnRemoteUpdate();
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		throw new NotImplException("LocalMoveState.handleRemoteMove");
	}
}

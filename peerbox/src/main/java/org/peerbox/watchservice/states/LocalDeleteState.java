package org.peerbox.watchservice.states;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.FileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.ConflictHandler;
import org.peerbox.watchservice.IFileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the delete state handles all events which would like
 * to alter the state from "delete" to another state (or keep the current state) and decides
 * whether an transition into another state is allowed.
 * 
 * 
 * @author winzenried
 *
 */
public class LocalDeleteState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(LocalDeleteState.class);

	public LocalDeleteState(Action action) {
		super(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event: Local Delete -> Established ({})", action.getFilePath());
		return new EstablishedState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Local Delete Event: Stay in Local Delete ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Stay in Local Delete ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldFilePath) {
		logger.debug("Local Move Event: Delete -> Local Move ({} > {})", 
				oldFilePath, action.getFilePath());
		return new LocalMoveState(action, oldFilePath);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event: Local Delete -> Conflict ({})", action.getFilePath());

		Path fileInConflict = action.getFilePath();
		Path renamedFile = ConflictHandler.rename(fileInConflict);
		try {
			Files.move(fileInConflict, renamedFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileInConflict = renamedFile;
		logger.debug("Conflict handling complete.");

		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event: Local Delete -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Local Delete -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	/**
	 * If the delete state is considered as stable, the execute method will be invoked which eventually
	 * deletes the file with the corresponding Hive2Hive method
	 * 
	 * @param file The file which should be deleted
	 * @return 
	 * @throws InvalidProcessStateException 
	 */
	@Override
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException {
//		Path path = action.getFilePath();
//		logger.debug("Execute LOCAL DELETE: {}", path);
//		IProcessComponent process = fileManager.delete(path.toFile());
//		if(process != null){
//			process.attachListener(new FileManagerProcessListener());
//		}
		logger.trace("File {}: Should be removed from the selective synchronization!");
		return null;
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		return this;
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		// throw new NotImplException("LocalDeleteState.handleLocalCreate");
		return changeStateOnLocalCreate();// new EstablishedState(action);
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		throw new NotImplException("LocalDeleteState.handleLocalDelete");
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		logger.debug("Local Update has no effect. File: {}", action.getFilePath());
		throw new NotImplException("LocalDeleteState.handleLocalUpdate");
	}

	@Override
	public AbstractActionState handleLocalMove(Path newPath) {
		logger.debug("NEWPATH: {}", newPath);
		Path oldPath = action.getFilePath();
		
		IFileEventManager eventManager = action.getEventManager();
		action.getFile().setParentPath(newPath.getParent());
		action.getFile().setPath(newPath);
//		action.getFile().propagatePathChangeToChildren();
		System.out.println("oldPath: " + newPath);
		System.out.println("action.getFile().getPath(): " + action.getFile().getPath());
		eventManager.getFileTree().putComponent(newPath.toString(), action.getFile());
		
		if(oldPath.equals(newPath)){
			logger.debug("This is a local move on place caused by the file system throwing DEL/ADD instead of MOD");
			eventManager.getFileComponentQueue().remove(action.getFile());
			return new EstablishedState(action);
		}
		updateTimeAndQueue();
		System.out.println("action.getFilePath(): " + action.getFilePath() + " action.getFile().getPath(): " + action.getFile().getPath());
		AbstractActionState newState = changeStateOnLocalMove(oldPath);
		return newState;
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		throw new NotImplException("LocalDeleteState.handleRemoteCreate");
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		throw new NotImplException("LocalDeleteState.handleRemoteDelete");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		throw new NotImplException("LocalDeleteState.handleRemoteUpdate");
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		throw new NotImplException("LocalDeleteState.handleRemoteMove");
	}

	public AbstractActionState getDefaultState() {
		logger.debug(
				"Return to default state 'InitialState' as component was removed from the tree: {}",
				action.getFilePath());
		return new InitialState(action);
	}
}

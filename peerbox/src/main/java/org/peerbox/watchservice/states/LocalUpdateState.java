package org.peerbox.watchservice.states;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.FileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.h2h.ProcessHandle;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.ConflictHandler;
import org.peerbox.watchservice.IFileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the modify state handles all events which would like
 * to alter the state from Modify to another state (or keep the current state) and decides
 * whether an transition into another state is allowed.
 * 
 * 
 * @author winzenried
 *
 */
public class LocalUpdateState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(LocalUpdateState.class);

	public LocalUpdateState(Action action) {
		super(action, StateType.LOCAL_UPDATE);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event: Stay in Local Update ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Local Delete Event: Local Update -> Local Delete ({})", action.getFilePath());
		return new LocalDeleteState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Stay in Local Update ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldPath) {
		logger.debug("Local Move Event: not defined");
		throw new IllegalStateException("Local Move Event: not defined");
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event: Local Update -> Conflict ({})", action.getFilePath());

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
		logger.debug("Remote Delete Event: Local Update -> Conflict ({})", action.getFilePath());

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
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Local Update -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		Path path = action.getFilePath();
		logger.debug("Execute LOCAL UPDATE: {}", path);
		ProcessHandle<Void> handle = fileManager.update(path.toFile());
		if (handle != null && handle.getProcess() != null) {
			handle.getProcess().attachListener(new FileManagerProcessListener());
			handle.executeAsync();
		} else {
			System.err.println("Process or handle is null.");
		}
		return new ExecutionHandle(action, handle);
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		throw new NotImplException("LocalUpdateState.handleLocalCreate");
	}

//	@Override
//	public AbstractActionState handleLocalDelete() {
//		// TODO Auto-generated method stub
//		
//		return changeStateOnLocalDelete();
//	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		// TODO Auto-generated method stub
		IFileEventManager eventManager = action.getEventManager();
		updateTimeAndQueue();
		return changeStateOnLocalUpdate();
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		throw new NotImplException("LocalUpdateState.handleLocalMove");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		throw new NotImplException("LocalUpdateState.handleRemoteCreate");
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		throw new NotImplException("LocalUpdateState.handleRemoteDelete");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		throw new NotImplException("LocalUpdateState.handleRemoteUpdate");
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		throw new NotImplException("LocalUpdateState.handleRemoteMove");
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleLocalRecover(int version) {
		// TODO Auto-generated method stub
		throw new NotImplException("LocalUpdateState.handleLocalRecover");
	}
}

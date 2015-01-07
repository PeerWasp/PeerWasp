package org.peerbox.watchservice.states;

import java.io.File;
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
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.peerbox.watchservice.filetree.composite.FileComponent;
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
		return new InitialState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Stay in Local Update ({})", action.getFilePath());
		return this;
	}
	
	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
//		logger.debug("Remote Update Event: Local Update -> Conflict ({})", action.getFilePath());
//
//		Path fileInConflict = action.getFilePath();
//		Path renamedFile = ConflictHandler.rename(fileInConflict);
//		try {
//			Files.move(fileInConflict, renamedFile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		fileInConflict = renamedFile;
//		logger.debug("Conflict handling complete.");
//
//		return new ConflictState(action);
		return new RemoteUpdateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event: Local Update -> Conflict ({})", action.getFilePath());
		return new LocalCreateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logStateTransission(getStateType(), EventType.REMOTE_MOVE, StateType.LOCAL_UPDATE);
		return this;
	}

	@Override
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		Path path = action.getFilePath();
		logger.debug("Execute LOCAL UPDATE: {}", path);
		handle = fileManager.update(path.toFile());
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
		return new RemoteCreateState(action);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		return changeStateOnLocalCreate();
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		// TODO Auto-generated method stub
		IFileEventManager eventManager = action.getEventManager();
		updateTimeAndQueue();
		return changeStateOnLocalUpdate();
	}


	@Override
	public AbstractActionState handleRemoteCreate() {
		ConflictHandler.resolveConflict(action.getFilePath());
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		return changeStateOnRemoteDelete();
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		ConflictHandler.resolveConflict(action.getFilePath());
		return changeStateOnRemoteUpdate();
	}

	//TODO write test-case for this!
	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		//Remove the file from the queue, move it in the tree, put it to the queue, move it on disk.
		Path srcPath = action.getFilePath();
		action.getEventManager().getFileComponentQueue().remove(action.getFile());
		FileComponent src = action.getEventManager().getFileTree().deleteFile(action.getFilePath());
		action.getEventManager().getFileTree().putFile(path, src);
		updateTimeAndQueue();
		if(Files.exists(srcPath)){
			try {
				Files.move(srcPath, path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return changeStateOnRemoteMove(path);
	}


}

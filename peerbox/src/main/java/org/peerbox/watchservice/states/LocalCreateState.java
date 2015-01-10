package org.peerbox.watchservice.states;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.h2h.ProcessHandle;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the create state handles all events which would like
 * to alter the state from "create" to another state (or keep the current state) and decides
 * whether an transition into another state is allowed.
 * 
 * 
 * @author winzenried
 *
 */
public class LocalCreateState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(LocalCreateState.class);

	public LocalCreateState(Action action) {
		super(action, StateType.LOCAL_CREATE);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event: Stay in Local Create ({})", action.getFile().getPath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Stay in Local Create ({})", action.getFile().getPath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldPath) {
		logger.debug("Local Move Event: not defined ({})", action.getFile().getPath());
		throw new IllegalStateException("Local Move Event: not defined");
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
//		logger.debug("Remote Update Event: Local Create -> Conflict ({})", action.getFile()
//				.getPath());
//
//		Path fileInConflict = action.getFile().getPath();
//		Path renamedFile = ConflictHandler.rename(fileInConflict);
//		try {
//			Files.move(fileInConflict, renamedFile);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		fileInConflict = renamedFile;
//		logger.debug("Conflict handling complete.");
//		return new ConflictState(action);
		return new RemoteUpdateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event: Local Create -> Conflict ({})", action.getFile()
				.getPath());
		return this;
	}

	/**
	 * If the create state is considered as stable, the execute method will be invoked which eventually
	 * uploads the file with the corresponding Hive2Hive method
	 * 
	 * @param file The file which should be uploaded
	 * @return 
	 * @return 
	 * @throws ProcessExecutionException
	 * @throws InvalidProcessStateException
	 * @throws NoPeerConnectionException
	 * @throws NoSessionException
	 */
	@Override
	public ExecutionHandle execute(FileManager fileManager) throws InvalidProcessStateException,
			ProcessExecutionException, NoSessionException, NoPeerConnectionException {
		Path path = action.getFile().getPath();
		logger.debug("Execute LOCAL CREATE: {}", path);
		handle = fileManager.add(path.toFile());
		if (handle != null && handle.getProcess() != null) {
			handle.getProcess().attachListener(new FileManagerProcessListener());
			handle.executeAsync();
		} else {
			System.err.println("process or handle is null");
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
	public AbstractActionState handleLocalMove(Path oldPath) {
		return changeStateOnLocalMove(oldPath);
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
	
	@Override
	public AbstractActionState handleLocalHardDelete(){
		action.getEventManager().getFileTree().deleteFile(action.getFile().getPath());
		action.getEventManager().getFileComponentQueue().remove(action.getFile());
		return changeStateOnLocalHardDelete();
	}
	
	public AbstractActionState changeStateOnLocalHardDelete(){
		return new InitialState(action);
	}
	
	public void performCleanup(){
//		action.setIsUploaded(true);
	}
}

package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalHardDeleteState extends AbstractActionState{
	
	private final static Logger logger = LoggerFactory.getLogger(LocalHardDeleteState.class);
	
	public LocalHardDeleteState(Action action) {
		super(action, StateType.LOCAL_HARD_DELETE);
	}

	public AbstractActionState getDefaultState() {
		logger.debug("Return to default state 'InitialState' as component was removed completely {}",
				action.getFile().getPath());
		return new InitialState(action);
	}
	
	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		return new LocalUpdateState(action); //e.g. hard delete -> Ctrl + Z;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Stay in LocalHardDeleteState");
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		return new LocalUpdateState(action); //e.g. hard delete -> Ctrl + Z;
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		return new InitialState(action); //File has already been deleted, finish.
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		return new RemoteCreateState(action); // The network wins
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		return new RemoteCreateState(action); // The network wins
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		return this;
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		updateTimeAndQueue();
		return changeStateOnLocalCreate();
	}
	
	public AbstractActionState handleLocalDelete(){
		logger.trace("File {}: entered handleLocalDelete", action.getFile().getPath());
//		action.getEventManager().getFileTree().deleteFile(action.getFile().getPath());
		IFileEventManager eventManager = action.getEventManager();
		eventManager.getFileComponentQueue().remove(action.getFile());
		FileComponent comp = eventManager.getFileTree().deleteFile(action.getFile().getPath());
		updateTimeAndQueue();
		return changeStateOnLocalDelete();
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		updateTimeAndQueue();
		return changeStateOnLocalUpdate();
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldFilePath) {
		throw new NotImplException("LocalHardDeleteState.handleLocalMove()");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		updateTimeAndQueue();
		return changeStateOnRemoteCreate();
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		action.getEventManager().getFileComponentQueue().remove(action.getFile());
		return changeStateOnRemoteDelete();
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		updateTimeAndQueue();
		return changeStateOnRemoteUpdate();
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		logger.info("The file which was locally deleted has been moved remotely. RemoteCreate at destination"
				+ "of move operation initiated to download the file: {}", path);
		updateTimeAndQueue();
		FileComponent moveDest = action.getEventManager().getFileTree().getOrCreateFileComponent(path, action.getEventManager());
		action.getEventManager().getFileTree().putFile(path, moveDest);
		moveDest.getAction().handleRemoteCreateEvent();
		
		
		return changeStateOnRemoteMove(path);
	}

	@Override
	public ExecutionHandle execute(IFileManager fileManager) throws InvalidProcessStateException, ProcessExecutionException, NoSessionException, NoPeerConnectionException {
		Path path = action.getFile().getPath();
		logger.debug("Execute LOCAL DELETE: {}", path);
		handle = fileManager.delete(path.toFile());
		if (handle != null && handle.getProcess() != null) {
			handle.getProcess().attachListener(new FileManagerProcessListener());
			handle.executeAsync();
		} else {
			System.err.println("handle or process is null.");
		}
		
		return new ExecutionHandle(action, handle);
	}
}

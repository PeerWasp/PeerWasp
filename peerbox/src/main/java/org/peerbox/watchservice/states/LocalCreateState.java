package org.peerbox.watchservice.states;

import java.nio.file.Path;
import java.util.Map;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.states.listeners.LocalFileAddListener;
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

	public LocalCreateState(IAction action) {
		super(action, StateType.LOCAL_CREATE);
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logStateTransition(getStateType(), EventType.LOCAL_UPDATE, StateType.LOCAL_CREATE);
		return this;
	}
	
	@Override
	public AbstractActionState changeStateOnLocalHardDelete(){
		logStateTransition(getStateType(), EventType.LOCAL_HARD_DELETE, StateType.INITIAL);
		return new InitialState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logStateTransition(getStateType(), EventType.REMOTE_DELETE, StateType.LOCAL_CREATE);
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
	public ExecutionHandle execute(IFileManager fileManager) throws InvalidProcessStateException,
			ProcessExecutionException, NoSessionException, NoPeerConnectionException {

		final FileComponent file = action.getFile();
		final Path path = file.getPath();
		final MessageBus messageBus = action.getFileEventManager().getMessageBus();
		
		logger.debug("Execute LOCAL CREATE: {}", path);
		
		handle = fileManager.add(path);
		if (handle != null && handle.getProcess() != null) {
			FileHelper helper = new FileHelper(path, file.isFile());
			handle.getProcess().attachListener(new LocalFileAddListener(helper, messageBus));
			handle.executeAsync();
		} else {
			System.err.println("process or handle is null");
		}
		
		String contentHash = action.getFile().getContentHash();
		IFileTree fileTree = action.getFileEventManager().getFileTree();
		fileTree.getCreatedByContentHash().get(contentHash).remove(action.getFile());

		return new ExecutionHandle(action, handle);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		return changeStateOnLocalCreate();
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		ConflictHandler.resolveConflict(action.getFile().getPath());
		
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
		return changeStateOnRemoteUpdate();
	}

	@Override
	public AbstractActionState handleLocalHardDelete(){
		action.getFileEventManager().getFileTree().deleteFile(action.getFile().getPath());
		action.getFileEventManager().getFileComponentQueue().remove(action.getFile());
		return changeStateOnLocalHardDelete();
	}
}

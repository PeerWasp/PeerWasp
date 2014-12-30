package org.peerbox.watchservice.states;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.FileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.h2h.ProcessHandle;
import org.peerbox.watchservice.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class RemoteUpdateState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(RemoteUpdateState.class);

	public RemoteUpdateState(Action action) {
		super(action, StateType.REMOTE_UPDATE);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event in RemoteUpdateState!  ({})", action.getFilePath());
		logStateTransission(getStateType(), EventType.LOCAL_CREATE, StateType.REMOTE_UPDATE);
		return this;//new EstablishedState(action);
		//TODO: maybe we should return 'this.' as soon as the update is successful, transission into ESTABLISHED happens!
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logStateTransission(getStateType(), EventType.LOCAL_UPDATE, StateType.REMOTE_UPDATE);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logStateTransission(getStateType(), EventType.LOCAL_DELETE, StateType.REMOTE_UPDATE);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path newPath) {
		logger.debug("Cannot accept local move right now, since update is happening.");
		logStateTransission(getStateType(), EventType.LOCAL_MOVE, getStateType());
//		try {
//			Files.move(newPath.toFile(), action.getFilePath().toFile());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return new LocalMoveState(action, newPath);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event:  ({})", action.getFilePath());
		logStateTransission(getStateType(), EventType.REMOTE_UPDATE, StateType.REMOTE_UPDATE);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event:  ({})", action.getFilePath());
		throw new NotImplException("RemoteUpdate.remoteDelete");
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event:  ({})", action.getFilePath());
		throw new NotImplException("RemoteUpdate.remoteMove");
	}

//	@Override
//	public AbstractActionState getDefaultState(){
//		return this;
//	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
//		throw new NotImplException("RemoteUpdateState.handleLocalCreate");
		return changeStateOnLocalCreate();
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
//		throw new NotImplException("RemoteUpdateState.handleLocalUpdate");
		action.getFile().bubbleContentHashUpdate();
		return changeStateOnLocalUpdate();
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		
		action.getNextState().changeStateOnRemoteUpdate();
		return changeStateOnLocalMove(oldPath);
//		return handleLocalMove(oldPath);//throw new NotImplException("RemoteUpdateState.handleLocalMove");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		throw new NotImplException("RemoteUpdateState.handleRemoteCreate");
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		throw new NotImplException("RemoteUpdateState.handleRemoteDelete");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		throw new NotImplException("RemoteUpdateState.handleRemoteUpdate");
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		throw new NotImplException("RemoteUpdateState.handleRemoteMove");
	}

	@Override
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		Path path = action.getFilePath();
		logger.debug("Execute REMOTE UPDATE, download the file: {}", path);
//		FileChannel channel;
//		try {
//			channel = new RandomAccessFile(path.toFile(), "rw").getChannel();
//			action.setFileChannel(channel);
//			FileLock lock = channel.lock();
//		} catch ( IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		handle = fileManager.download(path.toFile());
		if (handle != null && handle.getProcess() != null) {
			handle.getProcess().attachListener(new FileManagerProcessListener());
			handle.executeAsync();
		} else {
			System.err.println("process or handle is null");
		}

		return new ExecutionHandle(action, handle);
		// notifyActionExecuteSucceeded();
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleLocalRecover(int version) {
		// TODO Auto-generated method stub
		throw new NotImplException("RemoteUpdateState.handleLocalRecover");
	}
	
	public void performCleanup(){
		//nothing to do by default!
//		try {
//			action.getFileLock().release();
//			action.getFileChannel().close();
//			action.setFileChannel(null);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}
}

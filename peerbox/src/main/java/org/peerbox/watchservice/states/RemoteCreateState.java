package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.apache.commons.lang3.NotImplementedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteCreateState extends AbstractActionState{

	private static final Logger logger = LoggerFactory.getLogger(RemoteCreateState.class);
	public RemoteCreateState(Action action) {
		super(action);
		// TODO Auto-generated constructor stub
	}
	
//	public AbstractActionState getDefaultState(){
//		logger.debug("Stay in default state 'RemoteCreateState': {}", action.getFile().getPath());
////		return this;
//		return new EstablishedState(action);
//	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event in RemoteCreateState!  ({}) {}", action.getFilePath(), action.hashCode());
//		return new ConflictState(action);
		return new EstablishedState(action);
//		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event:  ({})", action.getFilePath());
//		return new ConflictState(action);
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Local Delete Event in RemoteCreateState ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldPath) {
		logger.debug("Local Move Event:  ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event:  ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		logger.debug("Remote Delete Event:  ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event:  ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int versionToRecover) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		// TODO Auto-generated method stub
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		action.getFile().updateContentHash();
		return changeStateOnLocalCreate();
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("RemoteCreateState.handleLocalDelete");
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		// TODO Auto-generated method stub
		return changeStateOnLocalUpdate();
//		throw new NotImplementedException("RemoteCreateState.handleLocalUpdate");
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("RemoteCreateState.handleLocalMove");
	}

	@Override
	public AbstractActionState handleLocalRecover() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("RemoteCreateState.handleLocalRecover");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("RemoteCreateState.handleRemoteCreate");
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("RemoteCreateState.handleRemoteDelete");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("RemoteCreateState.handleRemoteUpdate");
	}

	@Override
	public AbstractActionState handleRemoteMove() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("RemoteCreateState.handleRemoteMove");
	}
	
	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation, InvalidProcessStateException {
		Path path = action.getFilePath();
		logger.debug("Execute REMOTE ADD, download the file: {}", path);
		IProcessComponent process = fileManager.download(path.toFile());
		if(process != null){
			process.attachListener(new FileManagerProcessListener());
		} else {
			System.err.println("process is null");
		}
		
		//notifyActionExecuteSucceeded();
	}

}

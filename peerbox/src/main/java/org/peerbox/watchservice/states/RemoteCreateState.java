package org.peerbox.watchservice.states;

import java.nio.file.Path;

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

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event in RemoteUpdateState!  ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event:  ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Local Delete Event in RemoteUpdateState ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldFilePath) {
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
		
		notifyActionExecuteSucceeded();
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
	public void handleLocalCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleLocalDelete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleLocalUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleLocalMove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleLocalRecover() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteDelete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteMove() {
		// TODO Auto-generated method stub
		
	}

}

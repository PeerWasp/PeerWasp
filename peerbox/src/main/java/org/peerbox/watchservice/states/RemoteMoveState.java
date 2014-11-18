package org.peerbox.watchservice.states;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteMoveState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(RemoteMoveState.class);

	private Path oldPath;
	public RemoteMoveState(Action action, Path oldPath) {
		super(action);
		this.oldPath = oldPath;
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event:  ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event:  ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Local Delete Event:  ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldFilePath) {
		logger.debug("Local Move Event:  ({})", action.getFilePath());
		return this;
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
	public AbstractActionState changeStateOnRemoteMove(Path oldPath) {
		logger.debug("Remote Move Event:  ({})", action.getFilePath());
		return this;
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation {
		Path path = action.getFilePath();
		logger.debug("Execute REMOTE MOVE: {}", path);
//		try {
//			Files.move(oldPath, path);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int versionToRecover) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		// TODO Auto-generated method stub
		return new ConflictState(action);
	}
//	
//	@Override
//	public AbstractActionState getDefaultState(){
//		logger.debug("Returned own state");
//		return this;
//	}

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

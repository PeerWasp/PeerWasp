package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.ConflictHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConflictState extends AbstractActionState {

	private final static Logger logger = LoggerFactory.getLogger(ConflictState.class);

	public ConflictState(Action action) {
		super(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldFilePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation {
		// TODO Auto-generated method stub
//		Path fileInConflict = action.getFilePath();
//		Path renamedFile = ConflictHandler.rename(fileInConflict);
//		fileInConflict = renamedFile;
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int versionToRecover) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleLocalDelete() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		return null;
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		return null;
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractActionState handleLocalRecover(int version) {
		return null;
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		return null;
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		return null;
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		return null;
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		return null;
		// TODO Auto-generated method stub
		
	}

}

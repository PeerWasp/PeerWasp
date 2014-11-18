package org.peerbox.watchservice.states;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.ConflictHandler;
import org.peerbox.watchservice.IActionEventListener;
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
		super(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		logger.debug("Local Create Event: Stay in Local Create ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		logger.debug("Local Delete Event: Local Create -> Initial ({})", action.getFilePath());
		return new InitialState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		logger.debug("Local Update Event: Stay in Local Create ({})", action.getFilePath());
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldFilePath) {
		logger.debug("Local Move Event: not defined ({})", action.getFilePath());
		throw new IllegalStateException("Local Move Event: not defined");
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		logger.debug("Remote Update Event: Local Create -> Conflict ({})", action.getFilePath());
		
		logger.debug("We should rename the file here!");
//		File oldFile = action.getFilePath().toFile();
//		Path newFile = Paths.get(oldFile.getParent() + File.separator + oldFile.getName() + "_conflict");
//		try {
//			Files.move(oldFile.toPath(), newFile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
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
		logger.debug("Remote Delete Event: Local Create -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		logger.debug("Remote Move Event: Local Create -> Conflict ({})", action.getFilePath());
		return new ConflictState(action);
	}

	/**
	 * If the create state is considered as stable, the execute method will be invoked which eventually
	 * uploads the file with the corresponding Hive2Hive method
	 * 
	 * @param file The file which should be uploaded
	 * @throws InvalidProcessStateException 
	 */
	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation, InvalidProcessStateException {
		Path path = action.getFilePath();
		logger.debug("Execute LOCAL CREATE: {}", path);
		IProcessComponent process = fileManager.add(path.toFile());
		if(process != null){
			process.attachListener(new FileManagerProcessListener());
		} else {
			System.err.println("process is null");
		}
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

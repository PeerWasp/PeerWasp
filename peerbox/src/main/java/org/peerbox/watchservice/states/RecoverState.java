package org.peerbox.watchservice.states;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.PeerboxVersionSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecoverState extends AbstractActionState{
	
	private final static Logger logger = LoggerFactory.getLogger(RecoverState.class);
	
	private int fVersionToRecover = 0;
	public RecoverState(Action action, int versionToRecover) {
		super(action);
		fVersionToRecover = versionToRecover;
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractActionState handleLocalCreateEvent() {
		// TODO Auto-generated method stub
		return new LocalCreateState(action);
	}

	@Override
	public AbstractActionState handleLocalDeleteEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleLocalUpdateEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleLocalMoveEvent(Path oldFilePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleRecoverEvent(int versionToRecover) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleRemoteDeleteEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState handleRemoteUpdateEvent() {
		// TODO Auto-generated method stub
		return new InitialState(action);
	}

	@Override
	public AbstractActionState handleRemoteMoveEvent(Path oldFilePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation, InvalidProcessStateException {
			
		Path path = action.getFilePath();
		logger.debug("Execute RECOVER: {}", path);
		File currentFile = path.toFile();
		
		try {
			IProcessComponent process;
			process = fileManager.recover(currentFile, new PeerboxVersionSelector(fVersionToRecover));
			if(process != null){
				process.attachListener(new FileManagerProcessListener());
			} else {
				System.err.println("process is null");
			}
			
		} catch (FileNotFoundException | IllegalArgumentException e) {
			logger.error("{} in RECOVER: {}", e.getClass().getName(), path);
			e.printStackTrace();
		}
		
	}

}

package org.peerbox.watchservice.states;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.peerbox.FileManager;
import org.peerbox.exceptions.NotImplException;
import org.peerbox.h2h.ProcessHandle;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.PeerboxVersionSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecoverState extends AbstractActionState{
	
	private final static Logger logger = LoggerFactory.getLogger(RecoverState.class);
	
	private int versionToRecover = 0;
	private File currentFile;
	public RecoverState(Action action, File currentFile, int versionToRecover) {
		super(action, StateType.LOCAL_RECOVER);
		this.versionToRecover = versionToRecover;
		this.currentFile = currentFile;
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		// TODO Auto-generated method stub
		return new LocalCreateState(action);
	}

	@Override
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
			
		Path path = action.getFilePath();
		logger.debug("Execute RECOVER: {}", path);
		try {
			handle = fileManager.recover(currentFile, new PeerboxVersionSelector(versionToRecover));
			if(handle != null && handle.getProcess() != null){
				handle.getProcess().attachListener(new FileManagerProcessListener());
				handle.executeAsync();
			} else {
				System.err.println("process or handle is null");
			}
			return new ExecutionHandle(action, handle);
		} catch ( IllegalArgumentException e) {
			logger.error("{} in RECOVER: {}", e.getClass().getName(), path);
			e.printStackTrace();
		}
		return null;
		
		
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		// TODO Auto-generated method stub
		return new ConflictState(action);
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		// TODO Auto-generated method stub
		return this;
	}
	
	@Override
	public AbstractActionState handleLocalUpdate(){
		return this;
	}
	@Override
	public AbstractActionState handleRemoteUpdate(){
		throw new NotImplException("A remote update is not valid in RecoverState");
	}
	
	public AbstractActionState getDefaultState(){
		System.out.println("Go to LOCALCREATE!");
		return new LocalCreateState(action);
	}
	

}

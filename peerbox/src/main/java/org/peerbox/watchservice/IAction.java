package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.concurrent.locks.Lock;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.states.AbstractActionState;
import org.peerbox.watchservice.states.ExecutionHandle;

public interface IAction {

	public void updateTimestamp();
	public AbstractActionState getCurrentState();
	public long getTimestamp();
	public void addEventListener(IActionEventListener listener);
	public Path getFilePath();
	public void setEventManager(IFileEventManager fileEventManager);
	
	public ExecutionHandle execute(FileManager fileManager) throws NoSessionException,
	NoPeerConnectionException, InvalidProcessStateException;
	public void handleLocalCreateEvent();
	public void handleLocalMoveEvent(Path filePath);
	public void handleLocalUpdateEvent();
	public void handleLocalDeleteEvent();
	public void handleLocalHardDeleteEvent();
	public void handleRemoteCreateEvent();
	public void handleRemoteDeleteEvent();
	public void handleRemoteUpdateEvent();
	public void handleRemoteMoveEvent(Path srcPath);
//	public void setPath(Path path);
	public void setIsUploaded(boolean isUploaded);
	public boolean getIsUploaded();
	public FileComponent getFile();
	public void setFile(FileComponent file);
	public int getExecutionAttempts();
	public void onSucceed();
	public void onFailed();
	
	public AbstractActionState getNextState();
	public boolean isExecuting();

	public boolean getChangedWhileExecuted();
	
	public Lock getLock();
}

package org.peerbox.watchservice;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.states.AbstractActionState;

public interface IAction {

	public void updateTimestamp();
	public AbstractActionState getCurrentState();
	public long getTimestamp();
	public void addEventListener(IActionEventListener listener);
	public Path getFilePath();
	public void setEventManager(IFileEventManager fileEventManager);
	
	public void execute(FileManager fileManager) throws NoSessionException,
	NoPeerConnectionException, IllegalFileLocation, InvalidProcessStateException;
	public void handleLocalCreateEvent();
	public void handleLocalMoveEvent(Path filePath);
	public void handleLocalUpdateEvent();
	public void handleLocalDeleteEvent();
	public void handleRemoteCreateEvent();
	public void handleRemoteDeleteEvent();
	public void handleRemoteUpdateEvent();
	public void handleRemoteMoveEvent(Path srcPath);
//	public void setPath(Path path);
	public void setIsUploaded(boolean isUploaded);
	public boolean getIsUploaded();
	public void handleRecoverEvent(int versionToRecover);
	public FileComponent getFile();
	public void setFile(FileComponent file);
	public int getExecutionAttempts();
	public void onSucceed();
}

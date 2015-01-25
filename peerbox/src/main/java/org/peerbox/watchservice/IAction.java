package org.peerbox.watchservice;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.states.AbstractActionState;
import org.peerbox.watchservice.states.ExecutionHandle;

public interface IAction {

	FileComponent getFile();
	void setFile(FileComponent file);

	long getTimestamp();
	void updateTimestamp();

	AbstractActionState getCurrentState();
	String getCurrentStateName();
	AbstractActionState getNextState();
	String getNextStateName();

	IFileEventManager getFileEventManager();
	void setFileEventManager(IFileEventManager fileEventManager);

	ExecutionHandle execute(IFileManager fileManager) throws NoSessionException,
	NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException;
	boolean isExecuting();
	int getExecutionAttempts();
	boolean getChangedWhileExecuted();

	void handleLocalCreateEvent();
	void handleLocalUpdateEvent();
	void handleLocalDeleteEvent();
	void handleLocalHardDeleteEvent();
	void handleLocalMoveEvent(Path filePath);

	void handleRemoteCreateEvent();
	void handleRemoteUpdateEvent();
	void handleRemoteDeleteEvent();
	void handleRemoteMoveEvent(Path srcPath);

	void onSucceeded();
	void onFailed();

}
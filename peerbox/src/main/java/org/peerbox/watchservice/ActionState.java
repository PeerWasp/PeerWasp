package org.peerbox.watchservice;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;

public interface ActionState {
	
	public ActionState handleCreateEvent();
	public ActionState handleDeleteEvent();
	public ActionState handleModifyEvent();
	public void execute(Path filePath) throws NoSessionException, NoPeerConnectionException, IllegalFileLocation;
}
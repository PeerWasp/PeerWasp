package org.peerbox.watchservice;

import java.io.File;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;

public interface ActionState {
	
	public ActionState handleCreateEvent();
	public ActionState handleDeleteEvent();
	public ActionState handleModifyEvent();
	public void execute(File file) throws NoSessionException, NoPeerConnectionException, IllegalFileLocation;
}
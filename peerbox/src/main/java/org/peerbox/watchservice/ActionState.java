package org.peerbox.watchservice;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.FileManager;

/**
 * Interface for different states of implemented state pattern
 * 
 * @author winzenried
 *
 */
public abstract class ActionState {

	protected Action action;

	public ActionState(Action action) {
		this.action = action;
	}

	public abstract ActionState handleCreateEvent();

	public abstract ActionState handleDeleteEvent();

	public abstract ActionState handleModifyEvent();

	public abstract ActionState handleMoveEvent(Path filePath);

	public abstract void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation;
}

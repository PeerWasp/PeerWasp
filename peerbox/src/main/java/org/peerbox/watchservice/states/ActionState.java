package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;

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

	public abstract ActionState handleLocalCreateEvent();

	public abstract ActionState handleLocalDeleteEvent();

	public abstract ActionState handleLocalModifyEvent();

	public abstract ActionState handleLocalMoveEvent(Path oldFilePath);

	public abstract void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation;
}

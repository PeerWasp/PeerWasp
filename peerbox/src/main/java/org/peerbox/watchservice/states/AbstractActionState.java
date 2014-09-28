package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;

/**
 * Interface for different states of implemented state pattern
 * 
 * @author winzenried
 *
 */
public abstract class AbstractActionState {

	protected Action action;

	public AbstractActionState(Action action) {
		this.action = action;
	}

	/*
	 * LOCAL event handlers
	 */
	public abstract AbstractActionState handleLocalCreateEvent();

	public abstract AbstractActionState handleLocalDeleteEvent();

	public abstract AbstractActionState handleLocalUpdateEvent();

	public abstract AbstractActionState handleLocalMoveEvent(Path oldFilePath);

	/*
	 * REMOTE event handlers
	 */
	public abstract AbstractActionState handleRemoteCreateEvent();

	public abstract AbstractActionState handleRemoteDeleteEvent();

	// TODO: H2H does not distinguish between add/update (file is just downloaded and replaced)
	public abstract AbstractActionState handleRemoteUpdateEvent();

	public abstract AbstractActionState handleRemoteMoveEvent(Path oldFilePath);

	public abstract void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation, InvalidProcessStateException;
}

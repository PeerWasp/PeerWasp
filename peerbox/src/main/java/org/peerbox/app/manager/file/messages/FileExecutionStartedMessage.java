package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.watchservice.states.StateType;

public final class FileExecutionStartedMessage extends AbstractFileMessage {

	private final StateType stateType;

	public FileExecutionStartedMessage(final FileInfo file, final StateType stateType) {
		super(file);
		this.stateType = stateType;
	}

	public StateType getStateType() {
		return stateType;
	}

}

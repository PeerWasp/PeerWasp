package org.peerbox.presenter.settings.synchronization.messages;

import java.nio.file.Path;

import org.peerbox.app.manager.file.AbstractFileMessage;
import org.peerbox.watchservice.states.StateType;

public class FileExecutionSucceededMessage extends AbstractFileMessage{

	private StateType stateType;
	public FileExecutionSucceededMessage(Path path, StateType stateType) {
		super(path);
		this.stateType = stateType;
	}
	
	public StateType getStateType(){
		return stateType;
	}
}

package org.peerbox.presenter.settings.synchronization.messages;

import org.peerbox.app.manager.file.AbstractFileMessage;
import org.peerbox.presenter.settings.synchronization.FileHelper;
import org.peerbox.watchservice.states.StateType;

public class FileExecutionSucceededMessage extends AbstractFileMessage{

	private StateType stateType;
	public FileExecutionSucceededMessage(FileHelper file, StateType stateType) {
		super(file);
		this.stateType = stateType;
	}
	
	public StateType getStateType(){
		return stateType;
	}
}

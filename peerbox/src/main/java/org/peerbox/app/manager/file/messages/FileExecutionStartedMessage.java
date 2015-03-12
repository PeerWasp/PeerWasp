package org.peerbox.app.manager.file.messages;

import org.peerbox.presenter.settings.synchronization.FileHelper;
import org.peerbox.watchservice.states.StateType;

public class FileExecutionStartedMessage extends AbstractFileMessage {

	private StateType stateType;
	public FileExecutionStartedMessage(final FileHelper file, final StateType stateType) {
		super(file);
		this.stateType = stateType;
	}
	
	
	public StateType getStateType(){
		return stateType;
	}

}

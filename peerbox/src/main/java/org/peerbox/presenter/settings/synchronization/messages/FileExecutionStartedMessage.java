package org.peerbox.presenter.settings.synchronization.messages;

import org.peerbox.app.manager.file.AbstractFileMessage;
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

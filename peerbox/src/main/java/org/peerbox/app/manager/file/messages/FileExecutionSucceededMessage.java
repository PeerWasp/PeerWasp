package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.watchservice.states.StateType;

public final class FileExecutionSucceededMessage extends AbstractFileMessage {

	private StateType stateType;
	private FileInfo srcFile = null;

	public FileExecutionSucceededMessage(FileInfo file, StateType stateType) {
		super(file);
		this.stateType = stateType;
	}

	public FileExecutionSucceededMessage(FileInfo srcFile, FileInfo dstFile, StateType stateType) {
		super(dstFile);
		this.srcFile = srcFile;
		this.stateType = stateType;
	}

	public StateType getStateType() {
		return stateType;
	}

	public FileInfo getSourceFile() {
		return srcFile;
	}

}

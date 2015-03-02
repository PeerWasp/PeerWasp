package org.peerbox.presenter.settings.synchronization.messages;

import org.peerbox.app.manager.file.AbstractFileMessage;
import org.peerbox.presenter.settings.synchronization.FileHelper;
import org.peerbox.watchservice.states.StateType;

public class FileExecutionSucceededMessage extends AbstractFileMessage{

	private StateType stateType;
	private FileHelper srcFile = null;;
	public FileExecutionSucceededMessage(FileHelper file, StateType stateType) {
		super(file);
		this.stateType = stateType;
//		this(file, null, stateType);
	}
	
	public FileExecutionSucceededMessage(FileHelper srcFile, FileHelper dstFile, StateType stateType) {
		super(dstFile);
		this.srcFile = srcFile;
		this.stateType = stateType;
	}
	
	public StateType getStateType(){
		return stateType;
	}
	
	public FileHelper getSourceFile(){
		return srcFile;
	}
}

package org.peerbox.app.manager.file;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public final class RemoteFileMovedMessage extends AbstractFileMessage {

	private FileHelper srcFile;
	
	public RemoteFileMovedMessage(FileHelper srcFile, FileHelper dstFile) {
		super(dstFile);
		this.srcFile = srcFile;
	}
	
	public FileHelper getSourceFile(){
		return srcFile;
	}
	
	public FileHelper getDestinationFile(){
		return getFile();
	}

}

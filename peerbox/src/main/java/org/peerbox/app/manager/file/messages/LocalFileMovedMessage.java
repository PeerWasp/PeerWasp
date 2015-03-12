package org.peerbox.app.manager.file.messages;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileMovedMessage extends AbstractFileMessage {

	private FileHelper srcFile;
	
	public LocalFileMovedMessage(FileHelper srcFile, FileHelper dstFile)  {
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

package org.peerbox.app.manager.file.messages;

import org.peerbox.app.manager.file.FileInfo;

public final class LocalFileMovedMessage extends AbstractFileMessage {

	private FileInfo srcFile;

	public LocalFileMovedMessage(FileInfo srcFile, FileInfo dstFile) {
		super(dstFile);
		this.srcFile = srcFile;
	}

	public FileInfo getSourceFile() {
		return srcFile;
	}

	public FileInfo getDestinationFile() {
		return getFile();
	}

}

package org.peerbox.app.manager.file;

import java.nio.file.Path;

public final class RemoteFileMovedMessage extends AbstractFileMessage {

	private Path srcPath;
	
	public RemoteFileMovedMessage(Path srcPath, Path dstPath) {
		super(dstPath);
	}
	
	public Path getSourcePath(){
		return srcPath;
	}
	
	public Path getDestinationPath(){
		return getPath();
	}

}
